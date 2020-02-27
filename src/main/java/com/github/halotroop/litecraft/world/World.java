package com.github.halotroop.litecraft.world;

import java.util.Random;
import java.util.function.LongConsumer;

import com.github.halotroop.litecraft.save.LitecraftSave;
import com.github.halotroop.litecraft.types.block.Block;
import com.github.halotroop.litecraft.world.block.BlockRenderer;
import com.github.halotroop.litecraft.world.gen.*;
import com.github.hydos.ginger.engine.elements.objects.Player;
import com.github.hydos.ginger.engine.math.vectors.Vector3f;
import com.github.hydos.ginger.engine.obj.ModelLoader;
import com.github.hydos.ginger.engine.render.models.TexturedModel;

import it.unimi.dsi.fastutil.longs.*;

public class World implements BlockAccess, WorldGenConstants
{
	private final Long2ObjectMap<Chunk> chunks;
	private final WorldModifier[] worldModifiers;
	private final ChunkGenerator chunkGenerator;
	private final BlockAccess genBlockAccess;
	private final LitecraftSave save;

	private final long seed;
	private final int dimension;

	public Player player;

	// This will likely become the main public constructor after we add dynamic chunkloading
	private World(long seed, Dimension<?> dim, LitecraftSave save)
	{
		this.chunks = new Long2ObjectArrayMap<>();
		this.seed = seed;
		this.chunkGenerator = dim.createChunkGenerator(seed);
		this.worldModifiers = dim.getWorldModifierArray();
		this.genBlockAccess = new GenWorld(this);
		this.save = save;
		this.dimension = dim.id;
	}

	public void spawnPlayer()
	{
		TexturedModel dirtModel = ModelLoader.loadGenericCube("block/cubes/soil/dirt.png");
		this.player = new Player(dirtModel, new Vector3f(0, 0, -3), 0, 180f, 0, new Vector3f(0.2f, 0.2f, 0.2f));
	}

	// this constructor will likely not be neccesary when we have dynamic chunkloading
	public World(long seed, int size, Dimension<?> dim, LitecraftSave save)
	{
		this(seed, dim, save);

		for (int i = (0 - (size/2)); i < (size/2); i++)
			for (int k = (0 - (size/2)); k < (size/2); k++)
				for (int y = -2; y < 0; ++y)
					this.getChunk(i, y, k).setRender(true);
	}

	public Chunk getChunk(int chunkX, int chunkY, int chunkZ)
	{
		Chunk chunk = this.chunks.computeIfAbsent(posHash(chunkX, chunkY, chunkZ), pos -> {
			Chunk readChunk = save.readChunk(chunkX, chunkY, chunkZ, this.dimension);
			return readChunk == null ? this.chunkGenerator.generateChunk(chunkX, chunkY, chunkZ) : readChunk;
		});

		if (chunk.isFullyGenerated()) return chunk;

		this.populateChunk(chunkX, chunkY, chunkZ, chunk.chunkStartX, chunk.chunkStartY, chunk.chunkStartZ);
		chunk.setFullyGenerated(true);
		return chunk;
	}

	/**
	 * @return whether the chunk was unloaded without errors. Will often, but not always, be equal to whether the chunk was already in memory.
	 */
	public boolean unloadChunk(int chunkX, int chunkY, int chunkZ)
	{
		long posHash = posHash(chunkX, chunkY, chunkZ);
		Chunk chunk = this.chunks.get(posHash);

		// If the chunk is not in memory, it does not need to be unloaded
		if (chunk == null) return false;

		// Otherwise save the chunk
		boolean result = this.save.saveChunk(chunk);
		this.chunks.remove(posHash);
		return result;
	}

	private void populateChunk(int chunkX, int chunkY, int chunkZ, int chunkStartX, int chunkStartY, int chunkStartZ)
	{
		Random rand = new Random(this.seed + 5828671L * (long) chunkX + -47245139L * (long) chunkY + 8972357 * (long) chunkZ);

		for (WorldModifier modifier : this.worldModifiers)
		{
			modifier.modifyWorld(this.genBlockAccess, rand, chunkStartX, chunkStartY, chunkStartZ);
		}
	}

	/**
	 * @return a chunk that has not neccesarily gone through chunk populating. Used in chunk populating to prevent infinite recursion.
	 */
	Chunk getGenChunk(int chunkX, int chunkY, int chunkZ)
	{ return this.chunks.computeIfAbsent(posHash(chunkX, chunkY, chunkZ), pos -> this.chunkGenerator.generateChunk(chunkX, chunkY, chunkZ)); }

	private static long posHash(int chunkX, int chunkY, int chunkZ)
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
	{
		
		return chunk;
	}

	public void render(BlockRenderer blockRenderer)
	{
		Chunk chunk = getChunk(0, -1, 0);
		if(chunk!= null) {
			blockRenderer.prepareModel(chunk.getBlockEntity(0, -2, 0).getModel());
			this.chunks.forEach((pos, c) -> c.render(blockRenderer));
			blockRenderer.unbindModel();
		}
	}

	public void unloadAllChunks()
	{
		LongList chunkPositions = new LongArrayList();
		if (this.chunks != null)
			this.chunks.forEach((pos, chunk) -> { // for every chunk in memory
				chunkPositions.add((long) pos); // add pos to chunk positions list for removal later
				this.save.saveChunk(chunk); // save chunk
			});

		chunkPositions.forEach((LongConsumer) (pos -> this.chunks.remove(pos))); // remove all chunks
	}
}
