package com.github.hydos.ginger.engine.fbo;

public abstract class FboCallbackHandler
{
	public void cleanUp()
	{}

	public abstract void render(int texture);

	public void resize()
	{}
}
