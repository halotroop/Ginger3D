package com.github.hydos.ginger.engine.openGL.postprocessing;

import org.lwjgl.opengl.*;

import com.github.hydos.ginger.engine.openGL.render.models.RawModel;
import com.github.hydos.ginger.engine.openGL.utils.Loader;

public class PostProcessing
{
	private static final float[] POSITIONS =
	{
		-1, 1, -1, -1, 1, 1, 1, -1
	};
	private static RawModel quad;
	private static ContrastChanger contrastChanger;

	public static void cleanUp()
	{ contrastChanger.cleanUp(); }

	public static void doPostProcessing(int colourTexture)
	{
		start();
		contrastChanger.render(colourTexture);
		end();
	}

	private static void end()
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}

	public static void init()
	{
		quad = Loader.loadToVAO(POSITIONS, 2);
		contrastChanger = new ContrastChanger();
	}

	private static void start()
	{
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}
}
