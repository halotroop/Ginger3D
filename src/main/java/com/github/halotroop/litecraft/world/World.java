package com.github.halotroop.litecraft.world;

import java.util.*;

import com.github.hydos.ginger.engine.render.renderers.ObjectRenderer;

public class World
{
	public List<Chunk> chunks;
	
	public World() {
		chunks = new ArrayList<Chunk>();
	}
	
	public void generateWorld() {
		
	}
	
	public void optimiseChunks() {
		for(Chunk c: chunks) {
			optimiseChunk(c);
		}
	}
	
	public void optimiseChunk(int ID) {
		Chunk chunk = chunks.get(ID);
		optimiseChunk(chunk);
	}
	
	//used for model combining and culling
	public Chunk optimiseChunk(Chunk chunk) {
		//TODO: use this
		
		return null;
	}

	public void render(ObjectRenderer entityRenderer)
	{
		for(Chunk chunk: chunks) {
			chunk.render(entityRenderer);
		}
	}
	
	
}
