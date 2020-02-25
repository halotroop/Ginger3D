package com.github.hydos.ginger.engine.fbo;

public abstract class FboCallbackHandler
{
	
	public abstract void render();
	
	public void resize() {}
	public void cleanup() {}
	
}
