package com.github.halotroop.litecraft.world.gen;

import java.util.Random;

import com.github.halotroop.litecraft.types.block.*;
import com.github.halotroop.litecraft.util.noise.OctaveSimplexNoise;
import com.github.halotroop.litecraft.world.Chunk;

public class OverworldChunkGenerator implements ChunkGenerator, WorldGenConstants
{
	public OverworldChunkGenerator(long seed, int dimension)
	{
		this.noise = new OctaveSimplexNoise(new Random(seed), 3, 250.0, 35.0, 10.0);
		this.dimension = dimension;
	}

	private final OctaveSimplexNoise noise;
	private final int dimension;

	@Override
	public Chunk generateChunk(int chunkX, int chunkY, int chunkZ)
	{
		Chunk chunk = new Chunk(chunkX, chunkY, chunkZ, this.dimension);
		for (int x = 0; x < CHUNK_SIZE; ++x)
		{
			double totalX = x + chunk.chunkStartX;
			for (int z = 0; z < CHUNK_SIZE; ++z)
			{
				int height = (int) this.noise.sample(totalX, chunk.chunkStartZ + z);
				for (int y = 0; y < CHUNK_SIZE; ++y)
				{
					int totalY = chunk.chunkStartY + y;
					Block block = Blocks.AIR;
					if (totalY < height - 3)
					{
						block = Blocks.DIRT;
					}
					else if (totalY < height)
					{ block = Blocks.STONE; }
					chunk.setBlock(x, y, z, block);
				}
			}
		}
		return chunk;
	}
}
