package com.github.halotroop.litecraft.world.gen;

import com.github.halotroop.litecraft.world.dimension.Dimension;

public class OverworldDimension extends Dimension<OverworldChunkGenerator>
{
	public OverworldDimension(int id)
	{ super(id); }

	@Override
	public OverworldChunkGenerator createChunkGenerator(long seed)
	{ return new OverworldChunkGenerator(seed, this.id); }
}