package com.github.halotroop.litecraft.world;

import java.util.function.ToIntFunction;

import org.joml.Vector3f;

import com.github.halotroop.litecraft.logic.DataStorage;
import com.github.halotroop.litecraft.types.block.*;
import com.github.halotroop.litecraft.world.block.BlockRenderer;
import com.github.halotroop.litecraft.world.gen.WorldGenConstants;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import tk.valoeghese.sod.*;

public class Chunk implements BlockAccess, WorldGenConstants, DataStorage
{
	/** @param x in-chunk x coordinate.
	 * @param  y in-chunk y coordinate.
	 * @param  z in-chunk z coordinate.
	 * @return   creates a long that represents a coordinate, for use as a key in maps. */
	private static long posHash(int x, int y, int z)
	{ return ((long) x & MAX_POS) | (((long) y & MAX_POS) << POS_SHIFT) | (((long) z & MAX_POS) << DOUBLE_SHIFT); }

	private final Block[] blocks = new Block[CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE];
	private BlockEntity[] blockEntities = new BlockEntity[CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE];
	private boolean render = false;
	public final int chunkX, chunkY, chunkZ;
	public final int chunkStartX, chunkStartY, chunkStartZ;
	private boolean fullyGenerated = false;
	public final int dimension;
	private boolean dirty = true;
	private BlockEntity[] renderedBlocks;

	public Chunk(int chunkX, int chunkY, int chunkZ, int dimension)
	{
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.chunkStartX = chunkX << POS_SHIFT;
		this.chunkStartY = chunkY << POS_SHIFT;
		this.chunkStartZ = chunkZ << POS_SHIFT;
		this.dimension = dimension;
	}

	public boolean doRender()
	{ return this.render; }

	public void setFullyGenerated(boolean fullyGenerated)
	{ this.fullyGenerated = fullyGenerated; }

	@Override
	public Block getBlock(int x, int y, int z)
	{
		if (x > CHUNK_SIZE || y > CHUNK_SIZE || z > CHUNK_SIZE || x < 0 || y < 0 || z < 0)
		{
			throw new RuntimeException("Block [" + x + ", " + y + ", " + z + ", " + "] out of chunk bounds!");
		}

		return blocks[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y];
	}

	public BlockEntity getBlockEntity(int x, int y, int z)
	{
		if (x > CHUNK_SIZE || y > CHUNK_SIZE || z > CHUNK_SIZE || x < 0 || y < 0 || z < 0)
		{
			throw new RuntimeException("Block [" + x + ", " + y + ", " + z + ", " + "] out of chunk bounds!");
		}

		return this.blockEntities[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y];
	}

	public void render(BlockRenderer blockRenderer)
	{

		if (render)
		{
			if (dirty)
			{
				dirty = false;
				renderedBlocks = new BlockEntity[CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE];
				for(int x = 0; x < CHUNK_SIZE; x++)
				{
					for(int y = 0; y < CHUNK_SIZE; y++)
					{
						for(int z = 0; z < CHUNK_SIZE; z++)
						{
							BlockEntity block = getBlockEntity(x, y, z);
							if (x == 0 || x == CHUNK_SIZE-1 || z == 0 || z == CHUNK_SIZE-1 || y == 0 || y == CHUNK_SIZE-1)
							{
								renderedBlocks[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y] = block;
								continue;
							}

							// check for air. Yes this is stupid, TODO fix this
							if (getBlockEntity(x-1, y, z) == null || getBlockEntity(x+1, y, z) == null ||
									getBlockEntity(x, y-1, z) == null || getBlockEntity(x, y+1, z) == null ||
									getBlockEntity(x, y, z-1) == null || getBlockEntity(x, y, z+1) == null)
							{
								renderedBlocks[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y] = block;
							}
						}
					}
				}
			}

			blockRenderer.render(renderedBlocks);
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
		this.blocks[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y] = block;
		if (this.render)
		{
			this.blockEntities[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y] =  new BlockEntity(block, new Vector3f(this.chunkStartX + x, this.chunkStartY + y, this.chunkStartZ + z));
		}
		dirty = true;
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
						Block block = this.blocks[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y];
						if (block.isVisible()) this.blockEntities[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y] =  new BlockEntity(block,
								new Vector3f(
										this.chunkStartX + x,
										this.chunkStartY + y,
										this.chunkStartZ + z));
					}
				}
			}
		}
		else if (this.render) // else if it has been changed to false
		{
			blockEntities = new BlockEntity[CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE];
		}
		this.render = render;
		dirty = true;
	}

	public boolean isFullyGenerated()
	{ return this.fullyGenerated; }

	@Override
	public void read(BinaryData data)
	{
		Int2ObjectMap<Block> palette = new Int2ObjectArrayMap<>();

		DataSection paletteData = data.get("palette");

		boolean readInt = true; // whether the thing from the palette to be read is int
		int intIdCache = 0;
		for (Object o : paletteData)
		{
			if (readInt)
			{
				intIdCache = (int) o;
				readInt = false;
			}
			else
			{
				palette.put(intIdCache, Block.getBlock((String) o));
				readInt = true;
			}
		}

		DataSection blockData = data.get("block");

		long posHash = 0L; // also the index
		for (int z = 0; z < CHUNK_SIZE; ++z) // z, y, x order for data saving and loading so we can use incremental pos hashes
		{
			for (int y = 0; y < CHUNK_SIZE; ++y)
			{
				for (int x = 0; x < CHUNK_SIZE; ++x)
				{
					blocks[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y] = palette.get(blockData.readInt((int) posHash));
					++posHash;
				}
			}
		}
	}

	private int nextId; // for saving

	@Override
	public void write(BinaryData data)
	{
		Object2IntMap<Block> palette = new Object2IntArrayMap<>(); // block to int id

		DataSection paletteData = new DataSection();
		DataSection blockData = new DataSection();

		long posHash = 0L;
		nextId = 0;

		ToIntFunction<Block> nextIdProvider = b -> nextId++;

		for (int z = 0; z < CHUNK_SIZE; ++z) // z, y, x order for data saving and loading so we can use incremental pos hashes
		{
			for (int y = 0; y < CHUNK_SIZE; ++y)
			{
				for (int x = 0; x < CHUNK_SIZE; ++x)
				{
					Block b = blocks[x*CHUNK_SIZE*CHUNK_SIZE + z*CHUNK_SIZE + y];
					blockData.writeInt(palette.computeIntIfAbsent(b, nextIdProvider));
					++posHash;
				}
			}
		}

		palette.forEach((b, id) -> {
			paletteData.writeInt(id);
			paletteData.writeString(b.identifier);
		});

		data.put("palette", paletteData);
		data.put("block", blockData);

		dirty = true;
	}
}
