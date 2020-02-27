package com.github.halotroop.litecraft.world;

import java.util.*;

import com.github.halotroop.litecraft.types.block.*;
import com.github.halotroop.litecraft.world.block.BlockAccess;
import com.github.halotroop.litecraft.world.gen.WorldGenConstants;
import com.github.hydos.ginger.engine.math.vectors.Vector3f;
import com.github.hydos.ginger.engine.render.renderers.ObjectRenderer;

import it.unimi.dsi.fastutil.longs.*;

public class Chunk implements BlockAccess, WorldGenConstants
{
	/** @param x in-chunk x coordinate.
	 * @param  y in-chunk y coordinate.
	 * @param  z in-chunk z coordinate.
	 * @return   creates a long that represents a coordinate, for use as a key in maps. */
	private static long posHash(int x, int y, int z)
	{ return ((long) x & MAX_POS) | (((long) y & MAX_POS) << POS_SHIFT) | (((long) z & MAX_POS) << DOUBLE_SHIFT); }
	
	List<BlockEntity> renderList;
	private final Long2ObjectMap<Block> blocks = new Long2ObjectArrayMap<>();
	private final Long2ObjectMap<BlockEntity> blockEntities = new Long2ObjectArrayMap<>();
	private boolean render = false;
	public final int chunkX, chunkY, chunkZ;
	public final int chunkStartX, chunkStartY, chunkStartZ;
	private boolean fullyGenerated = false;

	public Chunk(int chunkX, int chunkY, int chunkZ)
	{
		renderList = new ArrayList<BlockEntity>();
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.chunkStartX = chunkX << POS_SHIFT;
		this.chunkStartY = chunkY << POS_SHIFT;
		this.chunkStartZ = chunkZ << POS_SHIFT;
	}

	public boolean doRender()
	{ return this.render; }

	public void setFullyGenerated(boolean fullyGenerated)
	{ this.fullyGenerated = fullyGenerated; }

	@Override
	public Block getBlock(int x, int y, int z)
	{
		long hash = posHash(x, y, z);
		return this.blocks.get(hash);
	}

	public BlockEntity getBlockEntity(int x, int y, int z)
	{
		long hash = posHash(x, y, z);
		return this.blockEntities.get(hash);
	}

	public void render(ObjectRenderer renderer)
	{
		renderList.clear();
		if (render)
		{
			for(int i = 0; i < CHUNK_SIZE; i++) {
				for(int j = 0; j < CHUNK_SIZE; j++) {
					for(int k = 0; k < CHUNK_SIZE; k++) {
						BlockEntity block = getBlockEntity(i, j, k);
						renderList.add(block);
					}
				}
			}
			
			renderer.prepare();
			renderer.render(renderList);
		}
	}

	@Override
	public void setBlock(int x, int y, int z, Block block)
	{
		if (x > MAX_POS)
			x = MAX_POS;
		else if (x < 0) x = 0;
		if (y > MAX_POS)
			y = MAX_POS;
		else if (y < 0) y = 0;
		if (z > MAX_POS)
			z = MAX_POS;
		else if (z < 0) z = 0;
		long hash = posHash(x, y, z);
		this.blocks.put(hash, block);
		if (this.render)
		{
			// TODO remove current block entity from game data when this class is integrated with the game
			this.blockEntities.put(hash, new BlockEntity(block, new Vector3f(this.chunkStartX + x, this.chunkStartY + y, this.chunkStartZ + z)));
		}
	}

	public void setRender(boolean render)
	{
		if (render && !this.render) // if it has been changed to true
		{
			for (int x = 0; x < CHUNK_SIZE; ++x)
			{
				for (int y = 0; y < CHUNK_SIZE; ++y)
				{
					for (int z = 0; z < CHUNK_SIZE; ++z)
					{
						long hash = posHash(x, y, z);
						Block block = this.blocks.get(hash);
						if (block.visible) this.blockEntities.put(hash, new BlockEntity(block,
							new Vector3f(
								this.chunkStartX + x,
								this.chunkStartY + y,
								this.chunkStartZ + z)));
					}
				}
			}
		}
		else if (this.render) // else if it has been changed to false
		{
			int length = blockEntities.size();
			for (int i = length; i >= 0; --i)
			{ this.blockEntities.remove(i); }
		}
		this.render = render;
	}

	public boolean isFullyGenerated()
	{ return this.fullyGenerated; }
}
