package com.github.halotroop.litecraft.world;

import com.github.halotroop.litecraft.types.block.Block;
import com.github.halotroop.litecraft.world.gen.WorldGenConstants;

final class GenWorld implements BlockAccess, WorldGenConstants
{
	GenWorld(World parent)
	{
		this.parent = parent;
	}

	public final World parent;

	@Override
	public Block getBlock(int x, int y, int z)
	{ return this.parent.getGenChunk(x >> POS_SHIFT, y >> POS_SHIFT, z >> POS_SHIFT).getBlock(x & MAX_POS, y & MAX_POS, z & MAX_POS); }

	@Override
	public void setBlock(int x, int y, int z, Block block)
	{ this.parent.getGenChunk(x >> POS_SHIFT, y >> POS_SHIFT, z >> POS_SHIFT).setBlock(x & MAX_POS, y & MAX_POS, z & MAX_POS, block); }
}
