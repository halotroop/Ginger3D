package com.github.hydos.ginger.engine.openGL.terrain;

public class TerrainTexturePack
{
	private TerrainTexture backgroundTexture;
	private TerrainTexture rTexture;
	private TerrainTexture gTexture;
	private TerrainTexture bTexture;

	public TerrainTexturePack(TerrainTexture backgroundTexture, TerrainTexture rTexture, TerrainTexture gTexture,
		TerrainTexture bTexture)
	{
		this.backgroundTexture = backgroundTexture;
		this.rTexture = rTexture;
		this.gTexture = gTexture;
		this.bTexture = bTexture;
	}

	public TerrainTexture getBackgroundTexture()
	{ return backgroundTexture; }

	public TerrainTexture getbTexture()
	{ return bTexture; }

	public TerrainTexture getgTexture()
	{ return gTexture; }

	public TerrainTexture getrTexture()
	{ return rTexture; }
}
