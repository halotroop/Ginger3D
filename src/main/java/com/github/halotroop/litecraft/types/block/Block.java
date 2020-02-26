package com.github.halotroop.litecraft.types.block;

import com.github.hydos.ginger.engine.obj.ModelLoader;
import com.github.hydos.ginger.engine.render.models.TexturedModel;

public class Block
{
	public static final Block AIR = new Block(new Properties().visible(false));
	public static final Block DIRT = new Block("block/cubes/soil/dirt.png", new Properties());
	public static final Block STONE = new Block("block/cubes/stone/basic/gneiss.png", new Properties());
	
	public static class Properties
	{ // add properties to this builder!
		private boolean visible = true;
		private boolean fullCube = true;

		public Properties fullCube(boolean fullCube)
		{
			this.fullCube = fullCube;
			return this;
		}

		public boolean isFullCube()
		{ return fullCube; }

		public boolean isVisible()
		{ return visible; }

		public Properties visible(boolean visible)
		{
			this.visible = visible;
			return this;
		}
	}
	
	public final TexturedModel model;
	public final boolean visible;

	protected Block(Properties properties)
	{ this((TexturedModel) null, properties); }

	protected Block(String texture, Properties properties)
	{ this(ModelLoader.loadGenericCube(texture), properties); }

	protected Block(TexturedModel model, Properties properties)
	{
		this.model = model;
		this.visible = properties.visible;
	}
}
