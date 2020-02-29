package com.github.halotroop.litecraft.screens;

import org.joml.*;

import com.github.halotroop.litecraft.Litecraft;
import com.github.hydos.ginger.engine.api.*;
import com.github.hydos.ginger.engine.font.GUIText;
import com.github.hydos.ginger.engine.screen.Screen;

public class IngameHUD extends Screen
{
	private GUIText debugText;
	private GUIText positionText;
	private Ginger ginger3D = Ginger.getInstance();
	private Litecraft litecraft = Litecraft.getInstance();
	
	public IngameHUD()
	{
		debugText = ginger3D.registerText("Loading...", 2, new Vector2f(0, 0), 1f, true, "debugInfo");
		debugText.setBorderWidth(0.5f);
		positionText = ginger3D.registerText("Loading...", 2, new Vector2f(0, -0.1f), 1f, true, "debugInfo");
		positionText.setBorderWidth(0.5f);
	}
	
	@Override
	public void render()
	{
		
	}

	@Override
	public void tick()
	{
		Vector4i dbg = litecraft.dbgStats;
		debugText.setText("FPS: " + dbg.x() + " UPS: " + dbg.y + " TPS: " + dbg.z);
		positionText.setText("Position " + GingerRegister.getInstance().game.data.player.getPosition().toString());
	}

	@Override
	public void close()
	{
		
	}
	
}
