package com.github.halotroop.litecraft.screens;

import org.joml.*;

import com.github.hydos.ginger.engine.openGL.api.*;
import com.github.hydos.ginger.engine.openGL.font.GUIText;
import com.github.hydos.ginger.engine.openGL.io.Window;
import com.github.hydos.ginger.engine.openGL.screen.Screen;

public class ExitGameScreen extends Screen
{
	private GUIText infoText;
	private Ginger ginger3D = Ginger.getInstance();
	
	public ExitGameScreen()
	{
		infoText = ginger3D.registerText("Saving and exiting...", 3, new Vector2f(Window.getWidth() / 2, Window.getHeight() / 2), 1f, true, "info");
		infoText.setBorderWidth(0.5f);
	}
	
	@Override
	public void render()
	{
		
	}

	@Override
	public void tick()
	{}

	@Override
	public void close()
	{ infoText.remove(); }
}
