package com.github.hydos.ginger.engine.openGL.screen;

import java.util.List;

import com.github.hydos.ginger.engine.openGL.elements.GuiTexture;

public abstract class Screen
{
	public List<GuiTexture> elements;

	public abstract void render();  // FIXME: This never gets called!!!

	public abstract void tick();
	
	public abstract void close();
}
