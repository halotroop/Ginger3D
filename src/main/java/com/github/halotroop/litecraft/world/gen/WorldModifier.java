package com.github.halotroop.litecraft.world.gen;

import java.util.Random;

public interface WorldModifier {
	void modifyWorld(Random rand, int chunkStartX, int chunkStartY, int chunkStartZ);
}
