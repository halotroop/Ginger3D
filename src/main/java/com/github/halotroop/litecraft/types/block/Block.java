package com.github.halotroop.litecraft.types.block;

import java.util.*;

import com.github.hydos.ginger.engine.obj.ModelLoader;
import com.github.hydos.ginger.engine.render.models.TexturedModel;

public class Block
{
	public static class Properties
	{ // add properties to this builder!
		private boolean visible = true;
		private boolean fullCube = true;
		private float caveCarveThreshold = -1f; // cannot carve
		private final String identifier;

		public Properties(String identifier)
		{ this.identifier = identifier; }

		public Properties fullCube(boolean fullCube)
		{
			this.fullCube = fullCube;
			return this;
		}

		public Properties visible(boolean visible)
		{
			this.visible = visible;
			return this;
		}

		public Properties caveCarveThreshold(float threshold)
		{
			this.caveCarveThreshold = threshold;
			return this;
		}
	}

	public final TexturedModel model;
	private final boolean visible, fullCube;
	private final float caveCarveThreshold;
	public final String identifier;

	public boolean isFullCube()
	{ return this.fullCube; }

	public boolean isVisible()
	{ return this.visible; }

	public float getCaveCarveThreshold()
	{ return this.caveCarveThreshold; }

	protected Block(Properties properties)
	{ this((TexturedModel) null, properties); }

	protected Block(String texture, Properties properties)
	{ this(ModelLoader.loadGenericCube(texture), properties); }

	protected Block(TexturedModel model, Properties properties)
	{
		this.model = model;
		this.visible = properties.visible;
		this.fullCube = properties.fullCube;
		this.identifier = properties.identifier;
		this.caveCarveThreshold = properties.caveCarveThreshold;
		IDENTIFIER_TO_BLOCK.put(this.identifier, this);
	}

	public static final Block getBlock(String identifier)
	{ return IDENTIFIER_TO_BLOCK.get(identifier); }

	public static final Block getBlockOrAir(String identifier)
	{ return IDENTIFIER_TO_BLOCK.getOrDefault(identifier, Blocks.AIR); }

	private static final Map<String, Block> IDENTIFIER_TO_BLOCK = new HashMap<>();
}
