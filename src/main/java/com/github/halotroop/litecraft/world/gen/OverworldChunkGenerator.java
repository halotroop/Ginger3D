package com.github.halotroop.litecraft.world.gen;

import java.util.Random;

import com.github.halotroop.litecraft.types.block.Block;
import com.github.halotroop.litecraft.util.noise.OctaveSimplexNoise;
import com.github.halotroop.litecraft.world.Chunk;

public class OverworldChunkGenerator implements ChunkGenerator, WorldGenConstants
{
	public OverworldChunkGenerator(long seed)
	{
		this.noise = new OctaveSimplexNoise(new Random(seed), 3, 250.0, 35.0, 10.0);
	}

	private final OctaveSimplexNoise noise;

	@Override
	public Chunk generateChunk(int chunkX, int chunkY, int chunkZ)
	{
		Chunk chunk = new Chunk(chunkX, chunkY, chunkZ);

		for (int x = 0; x < CHUNK_SIZE; ++x) {
			double totalX = x + chunk.chunkStartX;

			for (int z = 0; z < CHUNK_SIZE; ++z) {
				int height = (int) this.noise.sample(totalX, (double) (chunk.chunkStartZ + z));

				for (int y = 0; y < CHUNK_SIZE; ++y) {
					int totalY = chunk.chunkStartY + y;
					Block block = Block.AIR;

					if (totalY < height - 3)
					{
						block = Block.DIRT;
					}
					else if (totalY < height)
					{
						block = Block.STONE;
					}

					chunk.setBlock(x, y, z, block);
				}
			}
		}

		return chunk;
	}

}
