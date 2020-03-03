package com.github.halotroop.litecraft.world.gen;

import com.github.halotroop.litecraft.world.*;

public interface ChunkGenerator
{
	Chunk generateChunk(World world, int chunkX, int chunkY, int chunkZ);
}
