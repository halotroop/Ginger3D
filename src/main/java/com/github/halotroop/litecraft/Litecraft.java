package com.github.halotroop.litecraft;

import java.util.Random;

import com.github.halotroop.litecraft.screens.TitleScreen;
import com.github.halotroop.litecraft.world.World;
import com.github.halotroop.litecraft.world.gen.Dimension;
import com.github.hydos.ginger.engine.api.*;
import com.github.hydos.ginger.engine.api.game.*;
import com.github.hydos.ginger.engine.cameras.*;
import com.github.hydos.ginger.engine.elements.objects.*;
import com.github.hydos.ginger.engine.font.FontType;
import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.math.vectors.Vector3f;
import com.github.hydos.ginger.engine.obj.ModelLoader;
import com.github.hydos.ginger.engine.obj.shapes.StaticCube;
import com.github.hydos.ginger.engine.render.MasterRenderer;
import com.github.hydos.ginger.engine.render.models.TexturedModel;
import com.github.hydos.ginger.engine.utils.Loader;
import com.github.hydos.ginger.main.settings.Constants;

import tk.valoeghese.gateways.client.io.*;

public class Litecraft extends Game
{
	private World world;
	private Ginger ginger3D;
	private static Litecraft INSTANCE;

	//temp stuff to test out fbo fixes
	int oldWindowWidth = Window.width;
	int oldWindowHeight = Window.height;

	public Litecraft()
	{
		INSTANCE = this;
		Constants.movementSpeed = 0.00005f;
		Constants.turnSpeed = 0.00006f;
		Constants.gravity = new org.joml.Vector3f(0, -0.0000000005f, 0);
		Constants.jumpPower = 0.00005f;
		Window.create(1200, 800, "LiteCraft", 60);
		KeyCallbackHandler.trackWindow(Window.window);
		MouseCallbackHandler.trackWindow(Window.window);

		setupKeybinds();

		GingerUtils.init();
		Window.setBackgroundColour(0.2f, 0.2f, 0.6f);
		TexturedModel dirtModel = ModelLoader.loadGenericCube("block/cubes/stone/brick/stonebrick.png");
		StaticCube.scaleCube(1f);
		Player player = new Player(dirtModel, new Vector3f(0, 0, -3), 0, 180f, 0, new Vector3f(0.2f, 0.2f, 0.2f));

		Camera camera = new FirstPersonCamera(player);

		player.isVisible = false;
		ginger3D = new Ginger();
		data = new GameData(player, camera, 30);
		data.handleGuis = false;
		ginger3D.setup(new MasterRenderer(camera), this);

		FontType font = new FontType(Loader.loadFontAtlas("candara.png"), "candara.fnt");
		ginger3D.setGlobalFont(font);

		Light sun = new Light(new Vector3f(100, 105, -100), new Vector3f(1.3f, 1.3f, 1.3f), new Vector3f(0.0001f, 0.0001f, 0.0001f));
		data.lights.add(sun);
		data.entities.add(player);

		//		GuiTexture title = new GuiTexture(Loader.loadTextureDirectly("/textures/guis/title.png"), new Vector2f(0, 0.8F), new Vector2f(0.25f, 0.1f));
		//		data.guis.add(title);
		oldWindowWidth = Window.width;
		oldWindowHeight = Window.height;
		//start the game loop
		ginger3D.startGame();
	}

	private void setupKeybinds()
	{
		Input.addPressCallback(Keybind.EXIT, this::exit);
	}

	@Override
	public void exit()
	{ ginger3D.cleanup(); }

	@Override
	public void render()
	{
		if(ginger3D.gingerRegister.currentScreen == null) {
			ginger3D.openScreen(new TitleScreen());
		}
		ginger3D.update(data);
		if (oldWindowHeight != Window.height || oldWindowWidth != Window.width)
		{
			ginger3D.contrastFbo.resizeFBOs();
		}
		oldWindowWidth = Window.width;
		oldWindowHeight = Window.height;
		ginger3D.gingerRegister.masterRenderer.renderShadowMap(data.entities, data.lights.get(0));
		if (this.world != null)
		{ ginger3D.renderWorld(this, this.world); }
		ginger3D.renderOverlays(this);
		ginger3D.postRender();
	}

	@Override
	public void update()
	{
		Input.invokeAllListeners();
		data.player.updateMovement();
	}

	public static Litecraft getInstance() {
		return INSTANCE;
	}

	public void onPlayButtonClick() {
		if (world == null)
		{
			world = new World(new Random().nextLong(), 10, Dimension.OVERWORLD);
		}		
	}
}