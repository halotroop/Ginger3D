package com.github.halotroop.litecraft.world.dimension;

import com.github.halotroop.litecraft.world.gen.*;
import com.github.halotroop.litecraft.world.gen.modifier.CavesModifier;

public final class Dimensions
{
	public static final Dimension<EarthChunkGenerator> OVERWORLD = new EarthDimension(0, "earth");//.addWorldModifier(new CavesModifier());
}
