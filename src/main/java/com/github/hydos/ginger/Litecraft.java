package com.github.hydos.ginger;

import java.util.*;

import com.github.halotroop.litecraft.logic.Timer;
import com.github.halotroop.litecraft.logic.Timer.TickListener;
import com.github.halotroop.litecraft.types.block.*;
import com.github.hydos.ginger.engine.api.*;
import com.github.hydos.ginger.engine.api.game.*;
import com.github.hydos.ginger.engine.cameras.Camera;
import com.github.hydos.ginger.engine.elements.GuiTexture;
import com.github.hydos.ginger.engine.elements.buttons.TextureButton;
import com.github.hydos.ginger.engine.elements.objects.*;
import com.github.hydos.ginger.engine.font.*;
import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.math.vectors.*;
import com.github.hydos.ginger.engine.obj.ModelLoader;
import com.github.hydos.ginger.engine.obj.shapes.StaticCube;
import com.github.hydos.ginger.engine.render.MasterRenderer;
import com.github.hydos.ginger.engine.render.models.TexturedModel;
import com.github.hydos.ginger.engine.utils.Loader;
import com.github.hydos.ginger.main.settings.Constants;

public class Litecraft extends Game{
	
	private Ginger ginger3D;
	
	private boolean isInWorld = false;
	
	Timer timer;
	TickListener tickListener = new TickListener()
	{
		public void onTick(float deltaTime)
		{
			
		};
	};
	
	public Litecraft() {
		super();
		Constants.movementSpeed = 0.000005f;
		Constants.turnSpeed = 0.00002f;
		Constants.gravity = -0.000000000005f;
		Constants.jumpPower = 0.000005f;
		
		Window.create(1200, 800, "LiteCraft", 60);
		
		GingerUtils.init();
		
        Window.setBackgroundColour(0.2f, 0.2f, 0.8f);
        
		TexturedModel dirtModel = ModelLoader.loadGenericCube("block/cubes/soil/dirt.png");
        
        
        StaticCube.scaleCube(1);
		Player player = new Player(dirtModel, new Vector3f(0,0,-3),0,180f,0, new Vector3f(0.2f, 0.2f, 0.2f));
		Camera camera = new Camera(new Vector3f(0,0.1f,0), player);
		ginger3D = new Ginger();
		
		data = new GameData(player, camera);
		data.handleGuis = false;
		ginger3D.setup(new MasterRenderer(camera), this);
		
		
		float blockSpacing = 1f;
		float blockLineSpacing = 1f;
		float blockUpwardsSpacing = 1f;
		

		List<BlockEntity> chunk = new ArrayList<BlockEntity>();
		Block block = Block.DIRT;
		for(int k = 0; k<8;k++) {
			if(k == 7) {
				block = Block.GRASS;
			}
			for(int i = 0; i<8;i++) {
				for(int j = 0; j<8;j++) {
					chunk.add(new BlockEntity(block, new Vector3f(blockLineSpacing*i, blockUpwardsSpacing*k, blockSpacing*j)));
				}
			}
		}
		
		//add chunk to "entity" render list
		for(BlockEntity b: chunk) {
			data.entities.add(b);
		}
		
		
        FontType font = new FontType(Loader.loadFontAtlas("candara.png"), "candara.fnt");
        
        ginger3D.setGlobalFont(font);
        
        ginger3D.registerText("LiteCraft", 3, new Vector2f(0,0), 1f, true, "PLAYBUTTON");

        
		Light sun = new Light(new Vector3f(100,105,-100), new Vector3f(1.3f, 1.3f, 1.3f), new Vector3f(0.0001f, 0.0001f, 0.0001f));
		data.lights.add(sun);

		data.entities.add(player);
		
		TextureButton playButton = ginger3D.registerButton("/textures/guis/purpur.png", new Vector2f(0, 0), new Vector2f(0.25f, 0.1f));
		playButton.show(data.guis);
		
		GuiTexture title = new GuiTexture(Loader.loadTextureDirectly("/textures/guis/title.png"), new Vector2f(0, 0.8F), new Vector2f(0.25f, 0.1f));
		data.guis.add(title);
		
		
		//start the game loop
		ginger3D.startGame();
	}

	//temp stuff to test out fbo fixes
	int oldWindowWidth = Window.width;
	int oldWindowHeight = Window.height;
	
	@Override
	public void render() {
		ginger3D.update(data);
		
		if(oldWindowHeight != Window.height || oldWindowWidth != Window.width) {
			System.out.println("Windows size changed");
			ginger3D.contrastFbo.resizeFBOs();
		}
		
		oldWindowWidth = Window.width;
		oldWindowHeight = Window.height;
		
		ginger3D.masterRenderer.renderShadowMap(data.entities, data.lights.get(0));
		
		data.camera.move();
		data.player.move(null);
		
		if(isInWorld) {
			ginger3D.renderWithoutTerrain(this);
		}
		
		GUIText text = ginger3D.gingerRegister.texts.get(0);
		
		TextureButton playButton = ginger3D.gingerRegister.guiButtons.get(0);
		
		boolean isClicked = playButton.isClicked();
		
		playButton.update();
				
		text.setText(isClicked + "");
		ginger3D.renderOverlays(this);
		
		if(isClicked) {
			Window.lockMouse();
			playButton.hide(data.guis);
			isInWorld = true;
		}
		
		ginger3D.postRender();
	}


	@Override
	public void exit() {
		ginger3D.cleanup();
	}
}