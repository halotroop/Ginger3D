package com.github.halotroop.litecraft;

import org.joml.*;

import com.github.halotroop.litecraft.save.LitecraftSave;
import com.github.halotroop.litecraft.screens.*;
import com.github.halotroop.litecraft.types.block.Blocks;
import com.github.halotroop.litecraft.util.RelativeDirection;
import com.github.halotroop.litecraft.world.World;
import com.github.hydos.ginger.engine.common.Constants;
import com.github.hydos.ginger.engine.common.api.GingerRegister;
import com.github.hydos.ginger.engine.common.api.game.*;
import com.github.hydos.ginger.engine.common.cameras.*;
import com.github.hydos.ginger.engine.common.elements.objects.*;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.common.obj.ModelLoader;
import com.github.hydos.ginger.engine.common.obj.shapes.StaticCube;
import com.github.hydos.ginger.engine.openGL.api.*;
import com.github.hydos.ginger.engine.openGL.font.FontType;
import com.github.hydos.ginger.engine.openGL.render.MasterRenderer;
import com.github.hydos.ginger.engine.openGL.render.models.TexturedModel;
import com.github.hydos.ginger.engine.openGL.utils.GlLoader;

import tk.valoeghese.gateways.client.io.*;

public class Litecraft extends Game
{
	private static Litecraft INSTANCE;
	private World world;
	private LitecraftSave save;
	private GingerGL engine;
	public Player player;
	private Camera camera;
	public int fps, ups, tps;
	public Vector4i dbgStats = new Vector4i();
	private long frameTimer;

	public int threadWaitlist = 0;

	public Litecraft()
	{
		Litecraft.INSTANCE = this;
		// set constants
		this.setupConstants();
		this.setupGinger();
		Blocks.init(); // make sure blocks are initialised
		this.frameTimer = System.currentTimeMillis();
		setupKeybinds(); // setup keybinds
		// start the game loop
		this.engine.startGameLoop();
	}

	@Override
	public void exit()
	{
		engine.openScreen(new ExitGameScreen());
		render(); // Render the exit game screen
		if (this.world != null)
		{
			System.out.println("Saving chunks...");
			this.world.unloadAllChunks();
			this.getSave().saveGlobalData(this.world.getSeed(), this.player);
		}
		engine.cleanup();
		System.exit(0);
	}

	/**
	 * Things that ARE rendering: Anything that results in something being drawn to the frame buffer
	 * Things that are NOT rendering: Things that happen to update between frames but do not result in things being drawn to the screen
	 */
	@Override
	public void render()
	{
		fps += 1; // This section updates the debug stats once per real-time second, regardless of how many frames have been rendered
		if (System.currentTimeMillis() > frameTimer + 1000)
		updateDebugStats();
		// Render shadows
		GingerRegister.getInstance().masterRenderer.renderShadowMap(data.entities, data.lights.get(0));
		// If there's a world, render it!
		if (this.world != null) this.engine.renderWorld(this);
		// Render any overlays (GUIs, HUDs)
		this.engine.renderOverlays(this);
		// Put what's stored in the inactive framebuffer on the screen
		Window.swapBuffers();
	}
	
	// Updates the debug stats once per real-time second, regardless of how many frames have been rendered
	private void updateDebugStats()
	{
		this.dbgStats.set(fps, ups, tps, threadWaitlist);
		this.fps=0;
		this.ups=0;
		this.tps=0;
		this.frameTimer += 1000;
	}
	
	public void update()
	{
		Input.invokeAllListeners();
		data.player.updateMovement();
		data.camera.updateMovement();
	}

	private void setupConstants()
	{
		Constants.movementSpeed = 0.5f; // movement speed
		Constants.turnSpeed = 0.00006f; // turn speed
		Constants.gravity = new Vector3f(0, -0.0000000005f, 0); // compute gravity as a vec3f
		Constants.jumpPower = 0.00005f; // jump power
	}

	// set up Ginger3D engine stuff
	private void setupGinger()
	{
		if (engine == null) // Prevents this from being run more than once on accident.
		{
			this.setupWindow();
			GingerUtils.init(); // set up ginger utilities
			TexturedModel playerModel = ModelLoader.loadGenericCube("block/cubes/stone/brick/stonebrick.png");
			StaticCube.scaleCube(1f);
			Light sun = new Light(new Vector3f(0, 105, 0), new Vector3f(0.9765625f, 0.98828125f, 0.05859375f), new Vector3f(0.002f, 0.002f, 0.002f));
			FontType font = new FontType(GlLoader.loadFontAtlas("candara.png"), "candara.fnt");
			this.engine = new GingerGL();
			this.player = new Player(playerModel, new Vector3f(0, 0, -3), 0, 180f, 0, new Vector3f(0.2f, 0.2f, 0.2f));
			this.camera = new FirstPersonCamera(player);
			this.player.setVisible(false);
			this.data = new GameData(this.player, this.camera, 20);
			this.data.handleGuis = false;
			this.engine.setup(new MasterRenderer(this.camera), INSTANCE);
			this.engine.setGlobalFont(font);
			this.data.lights.add(sun);
			this.data.entities.add(this.player);
		}
	}

	private void setupKeybinds()
	{
		Input.addPressCallback(Keybind.EXIT, this::exit);
		Input.addInitialPressCallback(Keybind.FULLSCREEN, Window::fullscreen);
		Input.addInitialPressCallback(Keybind.WIREFRAME, GingerRegister.getInstance()::toggleWireframe);
		Input.addPressCallback(Keybind.MOVE_FORWARD, () -> this.player.move(RelativeDirection.FORWARD));
		Input.addPressCallback(Keybind.MOVE_BACKWARD, () -> this.player.move(RelativeDirection.BACKWARD));
		Input.addPressCallback(Keybind.STRAFE_LEFT, () -> this.player.move(RelativeDirection.LEFT));
		Input.addPressCallback(Keybind.STRAFE_RIGHT, () -> this.player.move(RelativeDirection.RIGHT));
		Input.addPressCallback(Keybind.FLY_UP, () -> this.player.move(RelativeDirection.UP));
		Input.addPressCallback(Keybind.FLY_DOWN, () -> this.player.move(RelativeDirection.DOWN));
	}
	
	private void setupWindow()
	{
		Window.create(1280, 720, "LiteCraft", 60); // create window
		KeyCallbackHandler.trackWindow(Window.getWindow()); // set up the gateways keybind key tracking
		MouseCallbackHandler.trackWindow(Window.getWindow());
	}

	/**
	 * Things that should be ticked: Entities when deciding an action, in-game timers (such as smelting), the in-game time
	 * Things that should not be ticked: Rendering, input, player movement
	 */ 
	@Override
	public void tick()
	{
		// Open the title screen if it's not already open.
		if (GingerRegister.getInstance().currentScreen == null && world == null)
			engine.openScreen(new TitleScreen());
	}
	
	// @formatter=off
	public static Litecraft getInstance()
	{ return INSTANCE; }

	public Camera getCamera()
	{ return this.camera; }

	public LitecraftSave getSave()
	{ return save; }

	public World getWorld()
	{ return this.world; }
	
	public void changeWorld(World world)
	{ this.world = world; }
	
	public void setSave(LitecraftSave save)
	{ this.save = save; }
}