package com.github.halotroop.litecraft.screens;

import org.joml.*;

import com.github.hydos.ginger.engine.common.font.GUIText;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.common.screen.Screen;
import com.github.hydos.ginger.engine.opengl.api.*;

public class ExitGameScreen extends Screen
{
	private GUIText infoText;
	private GingerGL ginger3D = GingerGL.getInstance();
	
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
	public void cleanup()
	{ infoText.remove(); }
}
