package com.github.halotroop.litecraft.types.block;

import com.github.halotroop.litecraft.types.block.Block.Properties;

public final class Blocks
{
	public static final Block AIR = new Block(new Properties("air").visible(false));
	public static final Block GRASS = new Block(new Properties("block/cubes/soil/grass/grass_top.png").canCaveCarve(true));
	public static final Block DIRT = new Block("block/cubes/soil/dirt.png", new Properties("dirt").canCaveCarve(true));
	public static final Block ANDESITE = new Block("block/cubes/stone/basic/andesite.png", new Properties("andesite").canCaveCarve(true));
	public static final Block DIORITE = new Block("block/cubes/stone/basic/diorite.png", new Properties("diorite").canCaveCarve(true));
	public static final Block GRANITE = new Block("block/cubes/stone/basic/granite.png", new Properties("granite").canCaveCarve(true));
	public static final Block GNEISS = new Block("block/cubes/stone/basic/gneiss.png", new Properties("gneiss").canCaveCarve(true));

	public static Block init()
	{
		return AIR;
	}
}
