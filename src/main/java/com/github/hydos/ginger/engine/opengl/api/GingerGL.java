package com.github.hydos.ginger.engine.opengl.api;

import org.joml.Vector2f;

import com.github.hydos.ginger.engine.common.api.GingerRegister;
import com.github.hydos.ginger.engine.common.api.game.Game;
import com.github.hydos.ginger.engine.common.elements.buttons.TextureButton;
import com.github.hydos.ginger.engine.common.elements.objects.RenderObject;
import com.github.hydos.ginger.engine.common.font.*;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.common.screen.Screen;
import com.github.hydos.ginger.engine.common.tools.MousePicker;
import com.github.hydos.ginger.engine.common.util.Timer;
import com.github.hydos.ginger.engine.common.util.Timer.TickListener;
import com.github.hydos.ginger.engine.opengl.postprocessing.*;
import com.github.hydos.ginger.engine.opengl.render.MasterRenderer;
import com.github.hydos.ginger.engine.opengl.utils.GLLoader;

public class GingerGL
{
	private static GingerGL INSTANCE;
	private GingerRegister registry;
	public MousePicker picker;
	public FontType globalFont;
	public Fbo contrastFbo;
	
	private Timer timer;
	TickListener gameTickListener = new TickListener()
	{
		@Override
		public void onTick(float deltaTime)
		{
			if (getRegistry().game != null) getRegistry().game.tick();
			if (getRegistry().currentScreen != null) getRegistry().currentScreen.tick();
		};
	};

	public void cleanup()
	{
		Window.stop();
		PostProcessing.cleanUp();
		getRegistry().masterRenderer.cleanUp();
		TextMaster.cleanUp();
		GLLoader.cleanUp();
	}

	public void openScreen(Screen screen)
	{
		if (getRegistry().currentScreen != null) getRegistry().currentScreen.cleanup();
		getRegistry().currentScreen = screen;
	}
	
	public void setGingerPlayer(RenderObject player)
	{
		registry.game.data.entities.remove(registry.game.player); // remove the old player
		registry.game.data.playerObject = player; // set all the player variables
		registry.game.player = player;
		registry.game.camera.player = player;
		registry.game.data.entities.add(player); // add the new player
	}

	public TextureButton registerButton(String resourceLocation, Vector2f position, Vector2f scale)
	{
		TextureButton button = new TextureButton(resourceLocation, position, scale);
		getRegistry().registerButton(button);
		return button;
	}

	public GUIText registerText(String string, int textSize, Vector2f position, float maxLineLength, boolean centered, String id)
	{
		GUIText text = new GUIText(string, textSize, globalFont, position, maxLineLength, false);
		text.textID = id;
		getRegistry().registerText(text);
		return text;
	}

	public void renderOverlays(Game game)
	{
		getRegistry().masterRenderer.renderGuis(game.data.guis);
		if (getRegistry().currentScreen != null) getRegistry().masterRenderer.renderGuis(getRegistry().currentScreen.elements);
		TextMaster.render();
	}

	public void setGlobalFont(FontType font)
	{ this.globalFont = font; }

	public void setup(MasterRenderer masterRenderer, Game game)
	{
		INSTANCE = this;
		registry = new GingerRegister();
		getRegistry().registerGame(game);
		timer = new Timer(game.data.tickSpeed);
		timer.addTickListener(gameTickListener);
		contrastFbo = new Fbo(new ContrastChanger());
		getRegistry().masterRenderer = masterRenderer;
		picker = new MousePicker(game.data.camera, masterRenderer.getProjectionMatrix());
		PostProcessing.init();
	}

	public void startGameLoop()
	{
		while (!Window.closed())
		{
			update(); // Run this regardless, (so as fast as possible)
			timer.tick(); // Run this only [ticklimit] times per second (This invokes gameTickListener.onTick!)
			if (Window.shouldRender()) getRegistry().game.render(); // Run this only [framelimit] times per second
		}
		getRegistry().game.exit();
	}

	// Things that should be run as often as possible, without limits
	public void update()
	{
		getRegistry().game.update();
		picker.update();
		GingerUtils.update();
		Window.update();
	}

	public static GingerGL getInstance()
	{ return INSTANCE; }

	public GingerRegister getRegistry()
	{
		return registry;
	}
}