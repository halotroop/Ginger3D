package io.github.halotroop.litecraft.types.block;

import io.github.hydos.ginger.engine.obj.ModelLoader;
import io.github.hydos.ginger.engine.render.models.TexturedModel;

public class Block
{
	protected Block(String texture, Properties properties)
	{
		this(ModelLoader.loadGenericCube(texture), properties);
	}

	protected Block(TexturedModel model, Properties properties)
	{
		this.model = model;
	}

	public final TexturedModel model;

	public static final Block GRASS = new Block("block/cubes/soil/gravel.png", new Properties());
	public static final Block DIRT = new Block("block/cubes/soil/dirt.png", new Properties());

	public static class Properties { // add properties to this builder!
		
	}
}
