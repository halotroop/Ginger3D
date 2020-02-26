package com.github.halotroop.litecraft.world.gen;

public interface WorldGenConstants
{
	static final int POS_SHIFT = 3;
	static final int DOUBLE_SHIFT = POS_SHIFT * 2;
	static final int CHUNK_SIZE = (int) Math.pow(2, POS_SHIFT);
	static final int MAX_POS = CHUNK_SIZE - 1;
}
