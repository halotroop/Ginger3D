package com.github.hydos.ginger.engine.fbo;

public abstract class FboCallbackHandler
{
	
	public abstract void render(int texture);
	
	public void resize() {}
	public void cleanUp() {}
	
}
