package com.github.halotroop.litecraft.world;

import com.github.halotroop.litecraft.types.block.Block;

import it.unimi.dsi.fastutil.longs.*;

public class World implements BlockAccess
{
	private final Long2ObjectMap<Chunk> chunks = new Long2ObjectArrayMap<>();

	public World(long seed)
	{
		// TODO world stuff
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
}
