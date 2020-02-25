package com.github.halotroop.litecraft.types.block;

import com.github.hydos.ginger.engine.obj.ModelLoader;
import com.github.hydos.ginger.engine.render.models.TexturedModel;

public class Block
{
	public static class Properties
	{ // add properties to this builder!
		private boolean visible = false;

		public Properties visible(boolean visible)
		{
			this.visible = visible;
			return this;
		}
	}

	public static final Block AIR = new Block((TexturedModel) null, new Properties().visible(false));

	public static final Block GRASS = new Block("block/cubes/soil/gravel.png", new Properties());
	public static final Block DIRT = new Block("block/cubes/soil/dirt.png", new Properties());
	public final TexturedModel model;
	public final boolean visible;
	protected Block(String texture, Properties properties)
	{ this(ModelLoader.loadGenericCube(texture), properties); }

	protected Block(TexturedModel model, Properties properties)
	{
		this.model = model;
		this.visible = properties.visible;
	}
}
