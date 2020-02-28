package com.github.halotroop.litecraft;

import java.io.IOException;

import org.joml.*;

import com.github.halotroop.litecraft.save.LitecraftSave;
import com.github.halotroop.litecraft.screens.TitleScreen;
import com.github.halotroop.litecraft.types.block.Blocks;
import com.github.halotroop.litecraft.util.RelativeDirection;
import com.github.halotroop.litecraft.world.World;
import com.github.halotroop.litecraft.world.gen.Dimensions;
import com.github.hydos.ginger.engine.api.*;
import com.github.hydos.ginger.engine.api.game.*;
import com.github.hydos.ginger.engine.cameras.*;
import com.github.hydos.ginger.engine.elements.objects.*;
import com.github.hydos.ginger.engine.font.FontType;
import com.github.hydos.ginger.engine.io.Window;
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
	private LitecraftSave save;
	private Ginger ginger3D;
	private static Litecraft INSTANCE;
	public Player player;
	private Camera camera;
	//temp stuff to test out fbo fixes
	int oldWindowWidth = Window.width;
	int oldWindowHeight = Window.height;
	public int fps, ups, tps, binds;
	public Vector4i dbgStats;
	private long frameTimer;

	public Litecraft()
	{
		Litecraft.INSTANCE = this;
		dbgStats = new Vector4i();
		// set constants
		this.setupConstants();
		this.setupWindow();
		Blocks.setup(); // make sure blocks are initialised
		GingerUtils.init(); // set up ginger utilities
		Window.setBackgroundColour(0.2f, 0.2f, 0.6f); // set the window refresh colour
		// set up Ginger3D stuff
		this.setupGinger();
		this.oldWindowWidth = Window.width;
		this.oldWindowHeight = Window.height;
		this.frameTimer = System.currentTimeMillis();
		setupKeybinds(); // set up keybinds
		//start the game loop
		this.ginger3D.startGame();
	}

	private void setupConstants()
	{
		Constants.movementSpeed = 0.5f; // movement speed
		Constants.turnSpeed = 0.00006f; // turn speed
		Constants.gravity = new Vector3f(0, -0.0000000005f, 0); // compute gravity as a vec3f
		Constants.jumpPower = 0.00005f; // jump power
	}

	private void setupWindow()
	{
		Window.create(1200, 800, "LiteCraft", 60); // create window
		KeyCallbackHandler.trackWindow(Window.window); // set up the gateways keybind key tracking
		MouseCallbackHandler.trackWindow(Window.window);
	}

	private void setupKeybinds()
	{
		Input.addPressCallback(Keybind.EXIT, this::exit);
		Input.addInitialPressCallback(Keybind.FULLSCREEN, Window::fullscreen);
		Input.addInitialPressCallback(Keybind.WIREFRAME, GingerRegister.getInstance()::toggleWireframe);
		Input.addPressCallback(Keybind.MOVE_FORWARD, () -> this.movePlayer(RelativeDirection.FORWARD));
		Input.addPressCallback(Keybind.MOVE_BACKWARD, () -> this.movePlayer(RelativeDirection.BACKWARD));
		Input.addPressCallback(Keybind.STRAFE_LEFT, () -> this.movePlayer(RelativeDirection.LEFT));
		Input.addPressCallback(Keybind.STRAFE_RIGHT, () -> this.movePlayer(RelativeDirection.RIGHT));
		Input.addPressCallback(Keybind.FLY_UP, () -> this.movePlayer(RelativeDirection.UP));
		Input.addPressCallback(Keybind.FLY_DOWN, () -> this.movePlayer(RelativeDirection.DOWN));
	}

	private void setupGinger() {
		TexturedModel playerModel = ModelLoader.loadGenericCube("block/cubes/stone/brick/stonebrick.png");
		StaticCube.scaleCube(1f);
		this.player = new Player(playerModel, new Vector3f(0, 0, -3), 0, 180f, 0, new Vector3f(0.2f, 0.2f, 0.2f));
		this.camera = new FirstPersonCamera(player);
		this.player.isVisible = false;
		this.ginger3D = new Ginger();
		this.data = new GameData(this.player, this.camera, 20);
		this.data.handleGuis = false;
		this.ginger3D.setup(new MasterRenderer(this.camera), INSTANCE);
		FontType font = new FontType(Loader.loadFontAtlas("candara.png"), "candara.fnt");
		this.ginger3D.setGlobalFont(font);
		Light sun = new Light(new Vector3f(100, 105, -100), new Vector3f(1.3f, 1.3f, 1.3f), new Vector3f(0.0001f, 0.0001f, 0.0001f));
		this.data.lights.add(sun);
		this.data.entities.add(this.player);
	}

	public Camera getCamera()
	{ return this.camera; }

	@Override
	public void exit()
	{
		if (this.world != null)
		{
			this.world.unloadAllChunks();
			try
			{
				this.save.saveGlobalData(this.world.getSeed(), this.player);
			}
			catch (IOException e)
			{
				System.err.println("A critical error occurred while trying to save world data!");
				e.printStackTrace();
			}
		}
		ginger3D.cleanup();
		System.exit(0);
	}

	private void movePlayer(RelativeDirection direction) {
		this.player.move(direction);
	}

	@Override
	public void render()
	{
		this.fps++;
		//FPS stuff sorry if i forget to remove whitespace
		if (System.currentTimeMillis() > frameTimer + 1000) // wait for one second
		{
			this.dbgStats.set(fps, ups, tps, 0);
			this.fps = 0;
			this.ups = 0;
			this.tps = 0;
			this.frameTimer += 1000; // reset the wait time
		}
		// TODO pls comment this code
		if (ginger3D.gingerRegister.currentScreen == null)
			this.ginger3D.openScreen(new TitleScreen());
		this.ginger3D.update(data);
		if (oldWindowHeight != Window.height || oldWindowWidth != Window.width)
			this.ginger3D.contrastFbo.resizeFBOs();
		this.oldWindowWidth = Window.width;
		this.oldWindowHeight = Window.height;
		this.ginger3D.gingerRegister.masterRenderer.renderShadowMap(data.entities, data.lights.get(0));
		if (this.world != null)
			this.ginger3D.renderWorld(this, this.world);
		this.ginger3D.renderOverlays(this);
		this.ginger3D.postRender();
		this.dbgStats.w = binds;
		this.binds = 0;
	}

	@Override
	public void tick()
	{
		tps++;
		Input.invokeAllListeners();
		data.player.updateMovement();
	}

	public static Litecraft getInstance()
	{ return INSTANCE; }

	public void onPlayButtonClick()
	{
		if (world == null)
		{
			this.save = new LitecraftSave("test", false);
			this.world = this.save.getWorldOrCreate(Dimensions.OVERWORLD);
			ginger3D.setGingerPlayer(this.world.player);
		}
	}
}