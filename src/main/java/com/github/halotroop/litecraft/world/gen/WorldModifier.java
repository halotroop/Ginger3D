package com.github.halotroop.litecraft.world.gen;

import java.util.Random;

import com.github.halotroop.litecraft.world.block.BlockAccess;

public interface WorldModifier {
	void modifyWorld(BlockAccess world, Random rand, int chunkStartX, int chunkStartY, int chunkStartZ);
}
