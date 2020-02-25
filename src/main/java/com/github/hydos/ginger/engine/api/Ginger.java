package com.github.hydos.ginger.engine.api;

import com.github.halotroop.litecraft.logic.Timer;
import com.github.halotroop.litecraft.logic.Timer.TickListener;
import com.github.halotroop.litecraft.world.World;
import com.github.hydos.ginger.engine.api.game.*;
import com.github.hydos.ginger.engine.elements.buttons.TextureButton;
import com.github.hydos.ginger.engine.font.*;
import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.math.vectors.Vector2f;
import com.github.hydos.ginger.engine.particle.ParticleMaster;
import com.github.hydos.ginger.engine.postprocessing.*;
import com.github.hydos.ginger.engine.render.MasterRenderer;
import com.github.hydos.ginger.engine.render.tools.MousePicker;
import com.github.hydos.ginger.engine.screen.Screen;
import com.github.hydos.ginger.engine.utils.Loader;

public class Ginger
{
	public MasterRenderer masterRenderer;
	public GingerRegister gingerRegister;
	public MousePicker picker;
	public FontType globalFont;
	public Fbo contrastFbo;
	Timer timer;
	TickListener gameTickListener = new TickListener()
	{
		@Override
		public void onTick(float deltaTime)
		{ gingerRegister.game.update(); };
	};

	public void cleanup()
	{
		Window.stop();
		PostProcessing.cleanUp();
		ParticleMaster.cleanUp();
		masterRenderer.cleanUp();
		TextMaster.cleanUp();
		Loader.cleanUp();
	}
	
	public void openScreen(Screen screen)
	{
		
	}

	public void postRender()
	{ Window.swapBuffers(); }

	public TextureButton registerButton(String resourceLocation, Vector2f position, Vector2f scale)
	{
		TextureButton button = new TextureButton(resourceLocation, position, scale);
		gingerRegister.registerButton(button);
		return button;
	}

	public GUIText registerText(String string, int textSize, Vector2f vector2f, float maxLineLength, boolean centered, String id)
	{
		GUIText text = new GUIText(string, textSize, globalFont, vector2f, maxLineLength, false);
		text.textID = id;
		gingerRegister.registerText(text);
		return text;
	}

	public void render(Game game)
	{
		GingerUtils.preRenderScene(masterRenderer);
		contrastFbo.bindFBO();
		masterRenderer.renderScene(game.data.entities, game.data.normalMapEntities, game.data.flatTerrains, game.data.lights, game.data.camera, game.data.clippingPlane);
		ParticleMaster.renderParticles(game.data.camera);
		contrastFbo.unbindFBO();
		contrastFbo.handler.render(contrastFbo.colorTexture);
		if (game.data.handleGuis)
		{ renderOverlays(game); }
	}

	public void renderOverlays(Game game)
	{
		masterRenderer.renderGuis(game.data.guis);
		TextMaster.render();
	}

	public void renderWithoutTerrain(Game game, World world)
	{
		GingerUtils.preRenderScene(masterRenderer);
		contrastFbo.bindFBO();
		masterRenderer.renderSceneNoTerrain(game.data.entities, game.data.normalMapEntities, game.data.lights, game.data.camera, game.data.clippingPlane, world);
		ParticleMaster.renderParticles(game.data.camera);
		contrastFbo.unbindFBO();
		PostProcessing.doPostProcessing(contrastFbo.colorTexture);
		if (game.data.handleGuis)
		{ renderOverlays(game); }
	}

	public void setGlobalFont(FontType font)
	{ this.globalFont = font; }

	public void setup(MasterRenderer masterRenderer, Game game)
	{
		gingerRegister = new GingerRegister();
		gingerRegister.registerGame(game);
		timer = new Timer(game.data.tickSpeed);
		timer.addTickListener(gameTickListener);
		contrastFbo = new Fbo(new ContrastChanger());
		this.masterRenderer = masterRenderer;
		picker = new MousePicker(game.data.camera, masterRenderer.getProjectionMatrix(), null);
		PostProcessing.init();
		ParticleMaster.init(masterRenderer.getProjectionMatrix());
	}

	public void startGame()
	{
		while (!Window.closed())
		{
			if (Window.isUpdating())
			{
				timer.tick();
				gingerRegister.game.render();
			}
		}
	}

	public void update(GameData data)
	{
		data.camera.move();
		Window.update();
		GingerUtils.update();
		picker.update();
		ParticleMaster.update(data.camera);
	}
}