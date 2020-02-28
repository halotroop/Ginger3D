package com.github.halotroop.litecraft;

import java.io.IOException;

import org.joml.Vector4i;
import org.lwjgl.glfw.GLFW;

import com.github.halotroop.litecraft.save.LitecraftSave;
import com.github.halotroop.litecraft.screens.TitleScreen;
import com.github.halotroop.litecraft.types.block.*;
import com.github.halotroop.litecraft.world.World;
import com.github.halotroop.litecraft.world.gen.Dimensions;
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
	private LitecraftSave save;
	private Ginger ginger3D;
	private static Litecraft INSTANCE;
	private Player player;
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
		Constants.movementSpeed = 0.5f;
		Constants.turnSpeed = 0.00006f;
		Constants.gravity = new org.joml.Vector3f(0, -0.0000000005f, 0);
		Constants.jumpPower = 0.00005f;
		Window.create(1200, 800, "LiteCraft", 60);
		KeyCallbackHandler.trackWindow(Window.window);
		MouseCallbackHandler.trackWindow(Window.window);
		setupKeybinds();

		@SuppressWarnings("unused")
		Block b = Blocks.AIR; // make sure blocks are initialised

		GingerUtils.init();
		Window.setBackgroundColour(0.2f, 0.2f, 0.6f);
		TexturedModel dirtModel = ModelLoader.loadGenericCube("block/cubes/stone/brick/stonebrick.png");
		StaticCube.scaleCube(1f);
		this.player = new Player(dirtModel, new Vector3f(0, 0, -3), 0, 180f, 0, new Vector3f(0.2f, 0.2f, 0.2f));
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
		this.oldWindowWidth = Window.width;
		this.oldWindowHeight = Window.height;
		this.frameTimer = System.currentTimeMillis();
		//start the game loop
		this.ginger3D.startGame();
	}

	private void setupKeybinds()
	{
		Input.addPressCallback(Keybind.EXIT, this::exit);
		Input.addPressCallback(Keybind.FULLSCREEN, Window::fullscreen);
	}

	@Override
	public void exit()
	{
		if (this.world != null)
		{
			this.world.unloadAllChunks();

			try
			{ this.save.saveGlobalData(this.world.getSeed(), this.player); }
			catch (IOException e)
			{
				System.err.println("A critical error occurred while trying to save world data!");
				e.printStackTrace();
			}
		}
	
		ginger3D.cleanup(); 
		System.exit(0);
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
		if (Window.isKeyDown(GLFW.GLFW_KEY_TAB))
			ginger3D.gingerRegister.wireframe = !ginger3D.gingerRegister.wireframe;
	}

	public static Litecraft getInstance()
	{ return INSTANCE; }

	public void setGingerPlayer(Player player)
	{
		this.data.entities.remove(this.player); // remove the old player

		this.data.player = player; // set all the player variables
		this.player = player;
		this.camera.player = player;

		this.data.entities.add(this.player); // add the new player
	}

	public void onPlayButtonClick()
	{
		if (world == null)
		{
			this.save = new LitecraftSave("test", false);
			this.world = this.save.getWorldOrCreate(Dimensions.OVERWORLD);
			this.setGingerPlayer(this.world.player);
		}		
	}
}