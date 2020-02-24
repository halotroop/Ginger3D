package com.github.halotroop.litecraft.world;

import com.github.halotroop.litecraft.types.block.Block;

public interface TileAccess
{
	Block getBlock(int x, int y, int z);
	void setBlock(int x, int y, int z, Block block);
}
