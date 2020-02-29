package com.github.halotroop.litecraft.world;

import java.util.*;
import java.util.function.LongConsumer;

import com.github.hydos.multiThreading.GingerThread;

import it.unimi.dsi.fastutil.longs.*;

public class DynamicChunkLoader extends GingerThread{
	
	public int chunkX, chunkY, chunkZ;
	public World world;
	
	public DynamicChunkLoader(int chunkX, int chunkY, int chunkZ, World world) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.world = world;
		this.setName("Dynamic Chunk thread");
	}
	
	@Override
	public void run() {
		Long2ObjectMap<Chunk> chunks = world.chunks; //this is to seperate the lists so we dont create render bugs
		
		started = true;
		List<Chunk> toKeep = new ArrayList<>();
		// loop over rendered area, adding chunks that are needed
		for (int x = chunkX - this.world.renderBound; x < chunkX + this.world.renderBound; x++)
			for (int z = chunkZ - this.world.renderBound; z < chunkZ + this.world.renderBound; z++)
				for (int y = chunkY - this.world.renderBound; y < chunkY + this.world.renderBound; y++)
					toKeep.add(this.world.getChunkToLoad(x, y, z));
		// list of keys to remove
		LongList toRemove = new LongArrayList();
		// check which loaded chunks are not neccesary
		chunks.forEach((pos, chunk) ->
		{
			if (!toKeep.contains(chunk))
				toRemove.add((long) pos);
		});
		// unload unneccesary chunks from chunk array
		toRemove.forEach((LongConsumer) pos -> this.world.unloadChunk(pos));
		// populate chunks to render if they are not rendered, then render them
		toKeep.forEach(chunk -> {
			if (!chunk.isFullyGenerated())
			{
				this.world.populateChunk(chunk);
				chunk.setFullyGenerated(true);
			}
			boolean alreadyRendering = chunk.doRender(); // if it's already rendering then it's most likely in the map
			chunk.setRender(true);
			if (!alreadyRendering)
				chunks.put(this.world.posHash(chunk.chunkX, chunk.chunkY, chunk.chunkZ), chunk);
		});
		this.world.chunks = chunks;
		this.finished = true;
	}
	
}
