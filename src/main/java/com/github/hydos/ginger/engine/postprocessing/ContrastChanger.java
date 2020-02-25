package com.github.hydos.ginger.engine.postprocessing;

import org.lwjgl.opengl.*;

import com.github.hydos.ginger.engine.fbo.FboCallbackHandler;

public class ContrastChanger extends FboCallbackHandler{
	
	private ImageRenderer renderer;
	private ContrastShader shader;
	
	public ContrastChanger() {
		shader = new ContrastShader();
		renderer = new ImageRenderer();
	}
	
	public void render(int texture) {
		shader.start();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		renderer.renderQuad();
		shader.stop();
	}
	
	public void cleanUp() {
		renderer.cleanUp();
		shader.cleanUp();
	}
	
}
