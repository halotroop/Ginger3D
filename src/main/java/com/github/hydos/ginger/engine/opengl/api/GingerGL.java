package com.github.hydos.ginger.engine.opengl.api;

import org.joml.Vector2f;

import com.github.halotroop.litecraft.Litecraft;
import com.github.halotroop.litecraft.logic.Timer;
import com.github.halotroop.litecraft.logic.Timer.TickListener;
import com.github.halotroop.litecraft.types.entity.PlayerEntity;
import com.github.hydos.ginger.engine.common.api.GingerRegister;
import com.github.hydos.ginger.engine.common.api.game.*;
import com.github.hydos.ginger.engine.common.elements.buttons.TextureButton;
import com.github.hydos.ginger.engine.common.font.*;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.common.screen.Screen;
import com.github.hydos.ginger.engine.common.tools.MousePicker;
import com.github.hydos.ginger.engine.opengl.postprocessing.*;
import com.github.hydos.ginger.engine.opengl.render.MasterRenderer;
import com.github.hydos.ginger.engine.opengl.utils.GlLoader;

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
			if (registry.game != null) registry.game.tick();
			if (registry.currentScreen != null) registry.currentScreen.tick();
		};
	};

	public void cleanup()
	{
		Window.stop();
		PostProcessing.cleanUp();
		registry.masterRenderer.cleanUp();
		TextMaster.cleanUp();
		GlLoader.cleanUp();
	}

	public void openScreen(Screen screen)
	{
		if (registry.currentScreen != null) registry.currentScreen.cleanup();
		registry.currentScreen = screen;
	}
	
	public void setGingerPlayer(PlayerEntity playerEntity)
	{
		registry.game.data.entities.remove(Litecraft.getInstance().playerEntity); // remove the old player
		registry.game.data.playerEntity = playerEntity; // set all the player variables
		Litecraft.getInstance().playerEntity = playerEntity;
		Litecraft.getInstance().getCamera().playerEntity = playerEntity;
		registry.game.data.entities.add(playerEntity); // add the new player
	}

	public TextureButton registerButton(String resourceLocation, Vector2f position, Vector2f scale)
	{
		TextureButton button = new TextureButton(resourceLocation, position, scale);
		registry.registerButton(button);
		return button;
	}

	public GUIText registerText(String string, int textSize, Vector2f position, float maxLineLength, boolean centered, String id)
	{
		GUIText text = new GUIText(string, textSize, globalFont, position, maxLineLength, false);
		text.textID = id;
		registry.registerText(text);
		return text;
	}

	public void renderOverlays(Game game)
	{
		registry.masterRenderer.renderGuis(game.data.guis);
		if (registry.currentScreen != null) registry.masterRenderer.renderGuis(registry.currentScreen.elements);
		TextMaster.render();
	}

	public void renderWorld(Litecraft game)
	{
		GameData data = game.data;
		GingerUtils.preRenderScene(registry.masterRenderer);
		contrastFbo.bindFBO();
		registry.masterRenderer.renderScene(data.entities, data.normalMapEntities, data.lights, data.camera, data.clippingPlane, game.getWorld());
		contrastFbo.unbindFBO();
		PostProcessing.doPostProcessing(contrastFbo.colorTexture);
		if (data.handleGuis) renderOverlays(game);
	}

	public void setGlobalFont(FontType font)
	{ this.globalFont = font; }

	public void setup(MasterRenderer masterRenderer, Game game)
	{
		INSTANCE = this;
		registry = new GingerRegister();
		registry.registerGame(game);
		timer = new Timer(game.data.tickSpeed);
		timer.addTickListener(gameTickListener);
		contrastFbo = new Fbo(new ContrastChanger());
		registry.masterRenderer = masterRenderer;
		picker = new MousePicker(game.data.camera, masterRenderer.getProjectionMatrix());
		PostProcessing.init();
	}

	public void startGameLoop()
	{
		while (!Window.closed())
		{
			update(Litecraft.getInstance().data); // Run this regardless, (so as fast as possible)
			if (timer.tick()) Litecraft.getInstance().tps += 1; // Run this only [ticklimit] times per second (This invokes gameTickListener.onTick!)
			if (Window.shouldRender()) registry.game.render(); // Run this only [framelimit] times per second
		}
		registry.game.exit();
	}

	// Things that should be run as often as possible, without limits
	public void update(GameData data)
	{
		registry.game.update();
		picker.update();
		GingerUtils.update();
		Window.update();
		Litecraft.getInstance().ups += 1;
	}

	public static GingerGL getInstance()
	{ return INSTANCE; }
}