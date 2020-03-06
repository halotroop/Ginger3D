package com.github.hydos.ginger.engine.common.screen;

import java.util.List;

import com.github.hydos.ginger.engine.common.elements.GLGuiTexture;

public abstract class Screen
{
	public List<GLGuiTexture> elements;

	public abstract void render();  // FIXME: This never gets called!!!

	public abstract void tick();
	
	public abstract void cleanup();
}
