package com.github.hydos.ginger.engine.api;

import java.util.*;

import com.github.hydos.ginger.engine.api.game.Game;
import com.github.hydos.ginger.engine.elements.buttons.TextureButton;
import com.github.hydos.ginger.engine.font.GUIText;
import com.github.hydos.ginger.engine.postprocessing.Fbo;
import com.github.hydos.ginger.engine.render.MasterRenderer;
import com.github.hydos.ginger.engine.screen.Screen;

/** Used if a game wants to access engine variables safely */
public class GingerRegister
{
	private static GingerRegister INSTANCE;
	public MasterRenderer masterRenderer;
	public static GingerRegister getInstance()
	{ return INSTANCE; }
	public List<GUIText> texts;
	public List<TextureButton> guiButtons;
	public List<Fbo> fbos;

	public Game game;
	public Screen currentScreen;
	public boolean wireframe = false;

	public GingerRegister()
	{ INSTANCE = this; }

	public void registerButton(TextureButton button)
	{
		if (guiButtons == null) guiButtons = new ArrayList<TextureButton>();
		guiButtons.add(button);
	}

	public void registerGame(Game game)
	{ this.game = game; }

	public void registerText(GUIText guiText)
	{
		if (texts == null) texts = new ArrayList<GUIText>();
		texts.add(guiText);
	}

	public void removeText(GUIText text)
	{ texts.remove(text); }

	public TextureButton retrieveButton(String string)
	{
		for (TextureButton button : guiButtons)
		{
			if (button.resourceLocation == string)
			{ return button; }
		}
		return null;
	}

	public GUIText retrieveText(String string)
	{
		for (GUIText text : texts)
		{
			if (string.equalsIgnoreCase(string))
			{ return text; }
		}
		return null;
	}
}