package com.github.halotroop.litecraft.world;

import com.github.halotroop.litecraft.types.block.Block;

public interface BlockAccess
{
	Block getBlock(int x, int y, int z);
	void setBlock(int x, int y, int z, Block block);

	static final int POS_SHIFT = 3;
	static final int DOUBLE_SHIFT = POS_SHIFT * 2;
	static final int CHUNK_SIZE = (int) Math.pow(2, POS_SHIFT);
	static final int MAX_POS = CHUNK_SIZE - 1;
}
