package com.github.halotroop.litecraft;

import java.util.Random;

import org.joml.Vector4i;
import org.lwjgl.glfw.GLFW;

import com.github.halotroop.litecraft.save.LitecraftSave;
import com.github.halotroop.litecraft.screens.TitleScreen;
import com.github.halotroop.litecraft.types.block.*;
import com.github.halotroop.litecraft.world.World;
import com.github.halotroop.litecraft.world.gen.*;
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

		Block b = Blocks.AIR; // make sure blocks are initialised

		GingerUtils.init();
		Window.setBackgroundColour(0.2f, 0.2f, 0.6f);
		TexturedModel dirtModel = ModelLoader.loadGenericCube("block/cubes/stone/brick/stonebrick.png");
		StaticCube.scaleCube(1f);
		Player player = new Player(dirtModel, new Vector3f(0, 0, -3), 0, 180f, 0, new Vector3f(0.2f, 0.2f, 0.2f));
		Camera camera = new FirstPersonCamera(player);
		player.isVisible = false;
		ginger3D = new Ginger();
		data = new GameData(player, camera, 20);
		data.handleGuis = false;
		ginger3D.setup(new MasterRenderer(camera), INSTANCE);
		FontType font = new FontType(Loader.loadFontAtlas("candara.png"), "candara.fnt");
		ginger3D.setGlobalFont(font);
		Light sun = new Light(new Vector3f(100, 105, -100), new Vector3f(1.3f, 1.3f, 1.3f), new Vector3f(0.0001f, 0.0001f, 0.0001f));
		data.lights.add(sun);
		data.entities.add(player);
		oldWindowWidth = Window.width;
		oldWindowHeight = Window.height;
		frameTimer = System.currentTimeMillis();
		//start the game loop
		ginger3D.startGame();
	}

	private void setupKeybinds()
	{ Input.addPressCallback(Keybind.EXIT, this::exit); }

	@Override
	public void exit()
	{
		this.world.unloadAllChunks();
		ginger3D.cleanup(); 
		System.exit(0);
	}

	@Override
	public void render()
	{
		fps++;
		//FPS stuff sorry if i forget to remove whitespace
		if (System.currentTimeMillis() > frameTimer + 1000) // wait for one second
		{
			this.dbgStats.set(fps, ups, tps, 0);
			this.fps = 0;
			this.ups = 0;
			this.tps = 0;
			this.frameTimer += 1000; // reset the wait time
		}
		if (ginger3D.gingerRegister.currentScreen == null)
			ginger3D.openScreen(new TitleScreen());
		ginger3D.update(data);
		if (oldWindowHeight != Window.height || oldWindowWidth != Window.width)
			ginger3D.contrastFbo.resizeFBOs();
		oldWindowWidth = Window.width;
		oldWindowHeight = Window.height;
		ginger3D.gingerRegister.masterRenderer.renderShadowMap(data.entities, data.lights.get(0));
		if (this.world != null)
			ginger3D.renderWorld(this, this.world);
		ginger3D.renderOverlays(this);
		ginger3D.postRender();
		dbgStats.w = binds;
		this.binds = 0;
	}

	@Override
	public void tick()
	{
		Input.invokeAllListeners();
		data.player.updateMovement();
		tps++;
		if (Window.isKeyDown(GLFW.GLFW_KEY_TAB))
			ginger3D.gingerRegister.wireframe = !ginger3D.gingerRegister.wireframe;
	}

	public static Litecraft getInstance()
	{ return INSTANCE; }

	public void onPlayButtonClick()
	{
		if (world == null)
		{
			this.save = new LitecraftSave("test", false);
			this.world = this.save.getWorldOrCreate(Dimensions.OVERWORLD);
		}		
	}
}