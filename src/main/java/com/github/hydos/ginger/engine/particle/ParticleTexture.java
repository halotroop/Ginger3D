package com.github.hydos.ginger.engine.particle;

public class ParticleTexture
{
	private int textureID;
	private int numberOfRows;

	public ParticleTexture(int textureID, int numberOfRows)
	{
		super();
		this.textureID = textureID;
		this.numberOfRows = numberOfRows;
	}

	public int getNumberOfRows()
	{ return numberOfRows; }

	public int getTextureID()
	{ return textureID; }
}
