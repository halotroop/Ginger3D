package com.github.halotroop.litecraft.screens;

import java.util.*;

import com.github.halotroop.litecraft.Litecraft;
import com.github.hydos.ginger.engine.api.*;
import com.github.hydos.ginger.engine.elements.GuiTexture;
import com.github.hydos.ginger.engine.elements.buttons.TextureButton;
import com.github.hydos.ginger.engine.font.*;
import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.math.vectors.Vector2f;
import com.github.hydos.ginger.engine.screen.Screen;

/*
 * YeS
 */
public class TitleScreen extends Screen
{	
	GUIText buildText;
	
	Ginger ginger3D;

	TextureButton playButton;
	
	public TitleScreen()
	{
		ginger3D = Ginger.getInstance();
		elements = new ArrayList<GuiTexture>();
		playButton = ginger3D.registerButton("/textures/guis/purpur.png", new Vector2f(0, 0), new Vector2f(0.25f, 0.1f));
		playButton.show(Litecraft.getInstance().data.guis);
		buildText = ginger3D.registerText("LiteCraft", 3, new Vector2f(0, 0), 1f, true, "PLAYBUTTON");
		buildText.setBorderWidth(0.5f);
	}
	
	@Override
	public void render()
	{
	}

	@Override
	public void tick()
	{
		playButton.update();
		if (playButton.isClicked())
		{
			buildText.remove();
			Window.lockMouse();
			playButton.hide(Litecraft.getInstance().data.guis);
			Litecraft.getInstance().onPlayButtonClick();//TODO: add world gui so it takes u to world creation place
			//TODO: also add a texture to be rendered behind the gui as an option
		}
	}
}
