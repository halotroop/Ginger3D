package com.github.halotroop.litecraft.world.gen;

public class OverworldDimension extends Dimension<OverworldChunkGenerator>
{
	@Override
	public OverworldChunkGenerator createChunkGenerator(long seed)
	{
		return new OverworldChunkGenerator(seed);
	}
}
