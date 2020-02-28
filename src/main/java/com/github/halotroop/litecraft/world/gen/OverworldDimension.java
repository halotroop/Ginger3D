package com.github.halotroop.litecraft.world.gen;

public class OverworldDimension extends Dimension<OverworldChunkGenerator>
{
	public OverworldDimension(int id)
	{ super(id); }

	@Override
	public OverworldChunkGenerator createChunkGenerator(long seed)
	{ return new OverworldChunkGenerator(seed, this.id); }
}