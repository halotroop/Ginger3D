package com.github.halotroop.litecraft.types.block;

import com.github.halotroop.litecraft.types.block.Block.Properties;

public final class Blocks
{
	public static final Block AIR = new Block(new Properties("air").visible(false).fullCube(false));
	public static final Block GRASS = new Block(new Properties("block/cubes/soil/grass/grass_top.png").caveCarveThreshold(0.11f));
	public static final Block DIRT = new Block("block/cubes/soil/dirt.png", new Properties("dirt").caveCarveThreshold(0.12f));
	public static final Block ANDESITE = new Block("block/cubes/stone/basic/andesite.png", new Properties("andesite").caveCarveThreshold(0.15f));
	public static final Block DIORITE = new Block("block/cubes/stone/basic/diorite.png", new Properties("diorite").caveCarveThreshold(0.18f));
	public static final Block GRANITE = new Block("block/cubes/stone/basic/granite.png", new Properties("granite").caveCarveThreshold(0.17f));
	public static final Block GNEISS = new Block("block/cubes/stone/basic/gneiss.png", new Properties("gneiss").caveCarveThreshold(0.14f));

	public static Block init()
	{
		return AIR;
	}
}
