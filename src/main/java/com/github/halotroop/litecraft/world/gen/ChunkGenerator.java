package com.github.halotroop.litecraft.world.gen;

import com.github.halotroop.litecraft.world.Chunk;

public interface ChunkGenerator
{
	Chunk generateChunk(int chunkX, int chunkY, int chunkZ);
}
