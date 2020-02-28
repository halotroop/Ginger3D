package com.github.halotroop.litecraft.world;

import java.util.*;
import java.util.function.LongConsumer;

import org.joml.Vector3f;

import com.github.halotroop.litecraft.save.LitecraftSave;
import com.github.halotroop.litecraft.types.block.*;
import com.github.halotroop.litecraft.world.block.BlockRenderer;
import com.github.halotroop.litecraft.world.dimension.Dimension;
import com.github.halotroop.litecraft.world.gen.*;
import com.github.hydos.ginger.engine.elements.objects.Player;
import com.github.hydos.ginger.engine.obj.ModelLoader;
import com.github.hydos.ginger.engine.render.models.TexturedModel;

import it.unimi.dsi.fastutil.longs.*;

public class World implements BlockAccess, WorldGenConstants
{
	final Long2ObjectMap<Chunk> chunks;
	private final WorldModifier[] worldModifiers;
	private final ChunkGenerator chunkGenerator;
	private final BlockAccess genBlockAccess;
	private final LitecraftSave save;
	private final long seed;
	private final int dimension;
	public Player player;
	int renderBound;
	int renderBoundVertical;
	private final BlockInstance dummy;

	public World(long seed, int renderSize, Dimension<?> dim, LitecraftSave save)
	{
		this.dummy = new BlockInstance(Blocks.ANDESITE, new Vector3f(0, 0, 0));
		this.dummy.isVisible = false;
		this.chunks = new Long2ObjectArrayMap<>();
		this.seed = seed;
		this.chunkGenerator = dim.createChunkGenerator(seed);
		this.worldModifiers = dim.getWorldModifierArray();
		this.genBlockAccess = new GenerationWorld(this);
		this.save = save;
		this.dimension = dim.id;
		this.renderBound = renderSize / 2;
		this.renderBoundVertical = this.renderBound / 2;
		if (this.renderBoundVertical < 2)
			this.renderBoundVertical = 2;
	}

	public int findAir(int x, int z)
	{
		int y = SEA_LEVEL;
		int attemptsRemaining = 255;

		while (attemptsRemaining --> 0)
		{
			// DO NOT CHANGE TO y++
			if (this.getBlock(x, ++y, z) == Blocks.AIR)
				return y;
		}

		return -1; // if it fails, returns -1
	}

	public void spawnPlayer()
	{
		int y = this.findAir(0, 0);
		if (y == -1)
			y = 300; // yeet

		this.spawnPlayer(0, y, -3);
	}

	public Player spawnPlayer(float x, float y, float z)
	{
		// Player model and stuff
		TexturedModel dirtModel = ModelLoader.loadGenericCube("block/cubes/soil/dirt.png");
		this.player = new Player(dirtModel, new Vector3f(x, y, z), 0, 180f, 0, new Vector3f(0.2f, 0.2f, 0.2f));
		this.player.isVisible = false;
		// Generate world around player
		long time = System.currentTimeMillis();
		System.out.println("Generating world!");
		this.updateLoadedChunks(this.player.getChunkX(), this.player.getChunkY(), this.player.getChunkZ());
		System.out.println("Generated world in " + (System.currentTimeMillis() - time) + " milliseconds");
		// return player
		return this.player;
	}

	public Chunk getChunk(int chunkX, int chunkY, int chunkZ)
	{
		Chunk chunk = this.chunks.computeIfAbsent(posHash(chunkX, chunkY, chunkZ), pos ->
		{
			Chunk readChunk = save.readChunk(chunkX, chunkY, chunkZ, this.dimension);
			return readChunk == null ? this.chunkGenerator.generateChunk(chunkX, chunkY, chunkZ) : readChunk;
		});
		if (chunk.isFullyGenerated()) return chunk;
		this.populateChunk(chunkX, chunkY, chunkZ, chunk.chunkStartX, chunk.chunkStartY, chunk.chunkStartZ);
		chunk.setFullyGenerated(true);
		return chunk;
	}

	public Chunk getChunkToLoad(int chunkX, int chunkY, int chunkZ)
	{
		// try get an already loaded chunk
		Chunk result = this.chunks.get(posHash(chunkX, chunkY, chunkZ));
		if (result != null)
			return result;
		// try read a chunk from memory
		result = save.readChunk(chunkX, chunkY, chunkZ, this.dimension);
		// if neither of those work, generate the chunk
		return result == null ? this.chunkGenerator.generateChunk(chunkX, chunkY, chunkZ) : result;
	}

	/** @return whether the chunk was unloaded without errors. Will often, but not always, be equal to whether the chunk was already in memory. */
	boolean unloadChunk(long posHash)
	{
		Chunk chunk = this.chunks.get(posHash);
		// If the chunk is not in memory, it does not need to be unloaded
		if (chunk == null) return false;
		// Otherwise save the chunk
		boolean result = this.save.saveChunk(chunk);
		this.chunks.remove(posHash);
		return result;
	}

	void populateChunk(Chunk chunk)
	{
		this.populateChunk(chunk.chunkX, chunk.chunkY, chunk.chunkZ, chunk.chunkStartX, chunk.chunkStartY, chunk.chunkStartZ);
	}

	private void populateChunk(int chunkX, int chunkY, int chunkZ, int chunkStartX, int chunkStartY, int chunkStartZ)
	{
		Random rand = new Random(this.seed + 5828671L * chunkX + -47245139L * chunkY + 8972357 * (long) chunkZ);
		for (WorldModifier modifier : this.worldModifiers)
		{ modifier.modifyWorld(this.genBlockAccess, rand, chunkStartX, chunkStartY, chunkStartZ); }
	}

	/** @return a chunk that has not neccesarily gone through chunk populating. Used in chunk populating to prevent infinite recursion. */
	Chunk getGenChunk(int chunkX, int chunkY, int chunkZ)
	{ return this.chunks.computeIfAbsent(posHash(chunkX, chunkY, chunkZ), pos -> this.chunkGenerator.generateChunk(chunkX, chunkY, chunkZ)); }

	long posHash(int chunkX, int chunkY, int chunkZ)
	{ return ((long) chunkX & 0x3FF) | (((long) chunkY & 0x3FF) << 10) | (((long) chunkZ & 0x3FF) << 20); }

	@Override
	public Block getBlock(int x, int y, int z)
	{ return this.getChunk(x >> POS_SHIFT, y >> POS_SHIFT, z >> POS_SHIFT).getBlock(x & MAX_POS, y & MAX_POS, z & MAX_POS); }

	@Override
	public void setBlock(int x, int y, int z, Block block)
	{ this.getChunk(x >> POS_SHIFT, y >> POS_SHIFT, z >> POS_SHIFT).setBlock(x & MAX_POS, y & MAX_POS, z & MAX_POS, block); }

	public void optimiseChunks()
	{ this.chunks.forEach((pos, chunk) -> optimiseChunk(chunk)); }

	//used for model combining and culling
	public Chunk optimiseChunk(Chunk chunk)
	{ return chunk; }

	public void render(BlockRenderer blockRenderer)
	{
		blockRenderer.prepareModel(this.dummy.getModel());
		this.chunks.forEach((pos, c) -> c.render(blockRenderer));
		blockRenderer.unbindModel();
	}

	public void unloadAllChunks()
	{
		LongList chunkPositions = new LongArrayList();
		if (this.chunks != null)
			this.chunks.forEach((pos, chunk) ->
			{ // for every chunk in memory
				chunkPositions.add((long) pos); // add pos to chunk positions list for removal later
				this.save.saveChunk(chunk); // save chunk
			});
		chunkPositions.forEach((LongConsumer) (pos -> this.chunks.remove(pos))); // remove all chunks
	}

	public long getSeed()
	{ return this.seed; }

	public static final int SEA_LEVEL = 0;

	public void updateLoadedChunks(int chunkX, int chunkY, int chunkZ)
	{
		DynamicChunkLoader chunkLoader = new DynamicChunkLoader(chunkX, chunkY, chunkZ, this);
		chunkLoader.start();
	}

	private static final class GenerationWorld implements BlockAccess, WorldGenConstants
	{
		GenerationWorld(World parent)
		{ this.parent = parent; }

		public final World parent;

		@Override
		public Block getBlock(int x, int y, int z)
		{ return this.parent.getGenChunk(x >> POS_SHIFT, y >> POS_SHIFT, z >> POS_SHIFT).getBlock(x & MAX_POS, y & MAX_POS, z & MAX_POS); }

		@Override
		public void setBlock(int x, int y, int z, Block block)
		{ this.parent.getGenChunk(x >> POS_SHIFT, y >> POS_SHIFT, z >> POS_SHIFT).setBlock(x & MAX_POS, y & MAX_POS, z & MAX_POS, block); }
	}
}
