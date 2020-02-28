package com.github.halotroop.litecraft.world.gen;

import java.util.*;

import it.unimi.dsi.fastutil.ints.*;

public abstract class Dimension<T extends ChunkGenerator>
{
	public List<WorldModifier> worldModifiers = new ArrayList<>();
	public final int id;

	public Dimension(int id)
	{
		this.id = id;
		ID_TO_DIMENSION.put(id, this);
	}

	public Dimension<T> addWorldModifier(WorldModifier modifier)
	{
		this.worldModifiers.add(modifier);
		return this;
	}

	public WorldModifier[] getWorldModifierArray()
	{ return this.worldModifiers.toArray(WorldModifier[]::new); }

	public abstract T createChunkGenerator(long seed);

	private static final Int2ObjectMap<Dimension<?>> ID_TO_DIMENSION = new Int2ObjectArrayMap<>();
}
