package com.github.halotroop.litecraft.world.gen;

import java.util.*;

import com.github.halotroop.litecraft.world.Chunk;

public abstract class Dimension<T extends ChunkGenerator>
{
	public List<WorldModifier> worldModifiers = new ArrayList<>();

	public Dimension addWorldModifier(WorldModifier modifier)
	{
		this.worldModifiers.add(modifier);
		return this;
	}

	public WorldModifier[] getWorldModifierArray() {
		return this.worldModifiers.toArray(new WorldModifier[0]);
	}

	public abstract T createChunkGenerator(long seed);

	public static final Dimension OVERWORLD = new OverworldDimension();
}
