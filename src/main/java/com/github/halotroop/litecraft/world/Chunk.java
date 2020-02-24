package com.github.halotroop.litecraft.world;

import java.util.ArrayList;
import java.util.List;

import com.github.halotroop.litecraft.types.block.*;
import com.github.hydos.ginger.engine.math.vectors.Vector3f;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public class Chunk
{
	public Chunk(int chunkX, int chunkY, int chunkZ)
	{
		this.chunkX = chunkX;
	}

	private final Long2ObjectMap<Block> blocks = new Long2ObjectArrayMap<>();
	private final List<BlockEntity> blockEntities = new ArrayList<>();
	private boolean render = false;
	
	public final int chunkX;

	public void setBlock(int x, int y, int z, Block block)
	{
		if (x > 7) x = 7;
		else if (x < 0) x = 0;
		if (y > 7) y = 7;
		else if (y < 0) y = 0;
		if (z > 7) z = 7;
		else if (z < 0) z = 0;

		long hash = posHash(x, y, z);
		this.blocks.put(hash, block);
	}

	public Block getBlock(int x, int y, int z)
	{
		long hash = posHash(x, y, z);
		return this.blocks.get(hash);
	}
	
	public void setRender(boolean render)
	{
		if (render && !this.render) // if it has been changed to true
		{
			this.blocks.forEach((coord, block) -> {
				if (block.visible) this.blockEntities.add(new BlockEntity(block, new Vector3f(0,0,0)));
			});
		}
		else if (this.render) // else if it has been changed to false
		{
			int length = blockEntities.size();

			for (int i = length; i >= 0; --i)
			{
				this.blockEntities.remove(i);
			}
		}

		this.render = render;
	}

	public boolean doRender()
	{ return this.render; }

	/**
	 * @param x in-chunk x coordinate.
	 * @param y in-chunk y coordinate.
	 * @param z in-chunk z coordinate.
	 * @return creates a long that represents a coordinate, for use as a key in maps.
	 */
	private static long posHash(int x, int y, int z)
	{ return ((long) x & 0b111) | (((long) y & 0b111) << 3) | (((long) z & 0b111) << 6); }
}
