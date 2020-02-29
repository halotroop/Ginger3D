package com.github.hydos.ginger.engine.api;

import org.joml.Vector2f;

import com.github.halotroop.litecraft.Litecraft;
import com.github.halotroop.litecraft.logic.Timer;
import com.github.halotroop.litecraft.logic.Timer.TickListener;
import com.github.hydos.ginger.engine.api.game.*;
import com.github.hydos.ginger.engine.elements.buttons.TextureButton;
import com.github.hydos.ginger.engine.elements.objects.Player;
import com.github.hydos.ginger.engine.font.*;
import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.particle.ParticleMaster;
import com.github.hydos.ginger.engine.postprocessing.*;
import com.github.hydos.ginger.engine.render.MasterRenderer;
import com.github.hydos.ginger.engine.render.tools.MousePicker;
import com.github.hydos.ginger.engine.screen.Screen;
import com.github.hydos.ginger.engine.utils.Loader;
import com.github.hydos.multithreading.GingerThreading;

public class Ginger
{
	private static Ginger INSTANCE;
	private GingerRegister registry;
	public MousePicker picker;
	public FontType globalFont;
	public Fbo contrastFbo;
	public GingerThreading threading;
	
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
		ParticleMaster.cleanUp();
		registry.masterRenderer.cleanUp();
		TextMaster.cleanUp();
		Loader.cleanUp();
	}

	public void openScreen(Screen screen)
	{
		if (registry.currentScreen != null) registry.currentScreen.close();
		registry.currentScreen = screen;
	}
	
	public void setGingerPlayer(Player player)
	{
		registry.game.data.entities.remove(Litecraft.getInstance().player); // remove the old player
		registry.game.data.player = player; // set all the player variables
		Litecraft.getInstance().player = player;
		Litecraft.getInstance().getCamera().player = player;
		registry.game.data.entities.add(player); // add the new player
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
		ParticleMaster.renderParticles(data.camera);
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
		threading = new GingerThreading();
		registry.registerGame(game);
		timer = new Timer(game.data.tickSpeed);
		timer.addTickListener(gameTickListener);
		contrastFbo = new Fbo(new ContrastChanger());
		registry.masterRenderer = masterRenderer;
		picker = new MousePicker(game.data.camera, masterRenderer.getProjectionMatrix());
		PostProcessing.init();
		ParticleMaster.init(masterRenderer.getProjectionMatrix());
	}

	public void startGameLoop()
	{
		if (!threading.isAlive()) // Prevents this from accidentally being run twice
		{
			threading.start();
			while (!Window.closed())
			{
				update(Litecraft.getInstance().data); // Run this regardless, (so as fast as possible)
				if (timer.tick()) Litecraft.getInstance().tps += 1; // Run this only [ticklimit] times per second (This invokes gameTickListener.onTick!)
				if (Window.shouldRender()) registry.game.render(); // Run this only [framelimit] times per second
			}
		}
		registry.game.exit();
	}

	// Things that should be run as often as possible, without limits
	public void update(GameData data)
	{
		registry.game.update();
		data.player.updateMovement();
		data.camera.updateMovement();
		picker.update();
		GingerUtils.update();
		ParticleMaster.update(data.camera);
		Window.update();
		Litecraft.getInstance().ups += 1;
	}

	public static Ginger getInstance()
	{ return INSTANCE; }
}