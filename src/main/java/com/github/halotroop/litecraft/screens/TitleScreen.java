package com.github.halotroop.litecraft.screens;

import java.util.*;

import com.github.hydos.ginger.engine.api.GingerRegister;
import com.github.hydos.ginger.engine.elements.GuiTexture;
import com.github.hydos.ginger.engine.screen.Screen;

/*
 * YeS
 */
public class TitleScreen extends Screen
{
	
	List<GuiTexture> elements;
	
	public TitleScreen()
	{
		elements = new ArrayList<GuiTexture>();
		//TODO: move over main menu ui elements from main class to here
	}
	
	@Override
	public void render()
	{
		GingerRegister.getInstance().masterRenderer.renderGuis(elements);
	}

	@Override
	public void tick()
	{
		
	}
	
	
	
}
