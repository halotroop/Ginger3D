package com.github.halotroop.litecraft;

import org.joml.*;

import com.github.halotroop.litecraft.save.LitecraftSave;
import com.github.halotroop.litecraft.screens.TitleScreen;
import com.github.halotroop.litecraft.types.block.Blocks;
import com.github.halotroop.litecraft.util.RelativeDirection;
import com.github.halotroop.litecraft.world.World;
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
	int oldWindowWidth = Window.getWidth();
	int oldWindowHeight = Window.getHeight();
	public int fps, ups, tps, binds;
	public Vector4i dbgStats;
	private long frameTimer;
	public int threadWaitlist = 0;

	public Litecraft()
	{
		Litecraft.INSTANCE = this;
		dbgStats = new Vector4i();
		// set constants
		this.setupConstants();
		this.setupWindow();
		Blocks.setup(); // make sure blocks are initialised
		GingerUtils.init(); // set up ginger utilities
		// set up Ginger3D stuff
		this.setupGinger();
		this.oldWindowWidth = Window.getWidth();
		this.oldWindowHeight = Window.getHeight();
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

	private void setupGinger()
	{
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
			System.out.println("Saving chunks...");
			this.world.unloadAllChunks();
			this.getSave().saveGlobalData(this.world.getSeed(), this.player);
		}
		ginger3D.cleanup();
		System.exit(0);
	}

	private void movePlayer(RelativeDirection direction)
	{ this.player.move(direction); }

	@Override
	public void render()
	{
		this.fps++;
		//FPS stuff sorry if i forget to remove whitespace
		if (System.currentTimeMillis() > frameTimer + 1000) // wait for one second
		{
			this.dbgStats.set(fps, ups, tps, threadWaitlist);
			this.fps = 0;
			this.ups = 0;
			this.tps = 0;
			this.frameTimer += 1000; // reset the wait time
		}
		// TODO: Document this code!
		if (ginger3D.gingerRegister.currentScreen == null) ginger3D.openScreen(new TitleScreen());
		this.ginger3D.update(data); // FIXME: This should either be renamed to "render" or moved to tick() if it has nothing to do with rendering.
		if (oldWindowHeight != Window.getHeight() || oldWindowWidth != Window.getWidth() && Window.getHeight() > 10 && Window.getWidth() > 10)
			this.ginger3D.contrastFbo.resizeFBOs();
		this.oldWindowWidth = Window.getWidth();
		this.oldWindowHeight = Window.getHeight();
		this.ginger3D.gingerRegister.masterRenderer.renderShadowMap(data.entities, data.lights.get(0));
		if (this.world != null) this.ginger3D.renderWorld(this, this.world);
		this.ginger3D.renderOverlays(this);
		this.ginger3D.postRender();
		this.binds = 0;
	}

	@Override
	public void tick()
	{
		this.tps++;
		Input.invokeAllListeners();
		data.player.updateMovement();
	}

	public static Litecraft getInstance()
	{ return INSTANCE; }

	public World getWorld()
	{ return this.world; }

	public LitecraftSave getSave()
	{ return save; }

	public void changeWorld(World world)
	{ this.world = world; }

	public void setSave(LitecraftSave save)
	{ this.save = save; }
}