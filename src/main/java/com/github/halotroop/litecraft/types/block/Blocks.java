package com.github.halotroop.litecraft.types.block;

import com.github.halotroop.litecraft.types.block.Block.Properties;

public final class Blocks
{
	public static final Block AIR = new Block(new Properties("air").visible(false));
	public static final Block DIRT = new Block("block/cubes/soil/dirt.png", new Properties("dirt"));
	public static final Block STONE = new Block("block/cubes/stone/basic/gneiss.png", new Properties("stone"));
	public static Block setup()
	{
		return AIR;
	}
}
