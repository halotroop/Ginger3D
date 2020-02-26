package com.github.halotroop.litecraft.world;

import com.github.halotroop.litecraft.types.block.Block;
import com.github.hydos.ginger.engine.elements.objects.Player;
import com.github.hydos.ginger.engine.math.vectors.Vector3f;
import com.github.hydos.ginger.engine.obj.ModelLoader;
import com.github.hydos.ginger.engine.render.models.TexturedModel;
import com.github.hydos.ginger.engine.render.renderers.ObjectRenderer;

import it.unimi.dsi.fastutil.longs.*;

public class World implements BlockAccess
{
	private final Long2ObjectMap<Chunk> chunks;
	public Player player;

	public World(long seed, int size)
	{
		chunks = new Long2ObjectArrayMap<>();
		
		for (int i = (0 - (size/2)); i < (size/2); i++)
			for (int k = (0 - (size/2)); k < (size/2); k++)
				this.getChunk(i, -1, k).setRender(true);
		
		TexturedModel dirtModel = ModelLoader.loadGenericCube("block/cubes/soil/dirt.png");
		this.player = new Player(dirtModel, new Vector3f(0, 0, -3), 0, 180f, 0, new Vector3f(0.2f, 0.2f, 0.2f));
	}

	public Chunk getChunk(int chunkX, int chunkY, int chunkZ)
	{ return this.chunks.computeIfAbsent(posHash(chunkX, chunkY, chunkZ), pos -> Chunk.generateChunk(chunkX, chunkY, chunkZ)); }

	private static long posHash(int x, int y, int z)
	{ return ((long) x & 0x3FF) | (((long) y & 0x3FF) << 10) | (((long) z & 0x3FF) << 20); }

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
		//TODO: use this
		return null;
	}

	public void render(ObjectRenderer entityRenderer)
	{ this.chunks.forEach((pos, chunk) -> chunk.render(entityRenderer)); }
}
