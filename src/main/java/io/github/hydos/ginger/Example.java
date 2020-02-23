package io.github.hydos.ginger;

import java.util.*;

import io.github.hydos.ginger.engine.api.*;
import io.github.hydos.ginger.engine.cameras.Camera;
import io.github.hydos.ginger.engine.elements.GuiTexture;
import io.github.hydos.ginger.engine.elements.buttons.TextureButton;
import io.github.hydos.ginger.engine.elements.objects.*;
import io.github.hydos.ginger.engine.font.*;
import io.github.hydos.ginger.engine.io.Window;
import io.github.hydos.ginger.engine.math.vectors.*;
import io.github.hydos.ginger.engine.obj.ModelLoader;
import io.github.hydos.ginger.engine.obj.shapes.StaticCube;
import io.github.hydos.ginger.engine.particle.*;
import io.github.hydos.ginger.engine.render.MasterRenderer;
import io.github.hydos.ginger.engine.render.models.TexturedModel;
import io.github.hydos.ginger.engine.terrain.*;
import io.github.hydos.ginger.engine.utils.Loader;
import io.github.hydos.ginger.main.GingerMain;
import io.github.hydos.ginger.main.settings.Constants;
import io.github.hydos.litecraft.Block;

public class Example extends Game{
	
	private Ginger ginger3D;
	
	public void main(String[] args) {
		//Render Player's constant variables
		Constants.movementSpeed = 0.000005f;
		Constants.turnSpeed = 0.00002f;
		Constants.gravity = -0.000000000005f;
		Constants.jumpPower = 0.000005f;
		
		Window.create(1200, 800, "LiteCraft", 60);
		
		GingerMain.init();
		
        Window.setBackgroundColour(0.2f, 0.2f, 0.8f);
        
		//TODO: block register class to register blockzz
		//TODO: could also probally pull the mesh from 1 place to lower memory usage in the future
		TexturedModel dirtModel = ModelLoader.loadGenericCube("block/cubes/soil/dirt.png");
		TexturedModel grassModel = ModelLoader.loadGenericCube("block/cubes/soil/dirt.png");
		
        
        
        
        StaticCube.scaleCube(6);
		Player player = new Player(dirtModel, new Vector3f(0,0,-3),0,180f,0, new Vector3f(0.2f, 0.2f, 0.2f));
		Camera camera = new Camera(new Vector3f(0,0.1f,0), player);
		ginger3D = new Ginger();
		
		data = new GameData(player, camera);
		data.handleGuis = false;
		ginger3D.setup(new MasterRenderer(data.camera), data);
		
		
		float blockSpacing = 1.2f;
		float blockLineSpacing = 1.2f;
		float blockUpwardsSpacing = 1.2f;
		
		//TODO: rename entity class to object class because not just entities
		List<Block> chunk = new ArrayList<Block>();
        //Basic chunk generation
		TexturedModel activeModel = dirtModel;
		for(int k = 0; k<8;k++) {
			if(k == 8) {
				activeModel = grassModel;
			}
			for(int i = 0; i<8;i++) {
				for(int j = 0; j<8;j++) {
					chunk.add(new Block(activeModel, new Vector3f(blockLineSpacing*i, blockUpwardsSpacing*k, blockSpacing*j)));
				}
			}
		}
		
		//add chunk to "entity" render list
		for(Block b: chunk) {
			data.entities.add(b);
		}

		
		
		
		//idk 
		
		
		
        FontType font = new FontType(Loader.loadFontAtlas("candara.png"), "candara.fnt");
        
        GUIText text = new GUIText("LiteCraft", 3, font, new Vector2f(0,0), 1f, true);
        text.setColour(0, 0, 0);        
        
		Terrain terrain = handleFlatTerrain();
		
		Light sun = new Light(new Vector3f(100,105,-100), new Vector3f(1.3f, 1.3f, 1.3f), new Vector3f(0.0001f, 0.0001f, 0.0001f));
		data.lights.add(sun);

		data.entities.add(player);	
		
		ParticleSystem system = setupParticles();
		
		boolean isInWorld = false;
		
		TextureButton playButton = new TextureButton("/textures/guis/purpur.png", new Vector2f(0, 0), new Vector2f(0.25f, 0.1f));
		playButton.show(data.guis);
		
		GuiTexture title = new GuiTexture(Loader.loadTextureDirectly("/textures/guis/title.png"), new Vector2f(0, 0.8F), new Vector2f(0.25f, 0.1f));
		data.guis.add(title);
		
		while(!Window.closed()) {
			
			if(Window.isUpdating()) {
				ginger3D.update(data);
				
				ginger3D.masterRenderer.renderShadowMap(data.entities, sun);
				
				camera.move();
				player.move(terrain);
				
				system.generateParticles(new Vector3f(0,-2,0));
				
				if(isInWorld) {
					ginger3D.renderWithoutTerrain(this);
//					TODO: dynamic text
//					text.textString = "" + (data.entities.size() + data.flatTerrains.size());
				}
				ginger3D.renderOverlays(this);
				
				playButton.update();
				if(playButton.isClicked()) {
					Window.lockMouse();
					playButton.hide(data.guis);
					isInWorld = true;
				}
				
				ginger3D.postRender();
			}
			
		}
		
	}


	private ParticleSystem setupParticles() {
		ParticleTexture particleTexture = new ParticleTexture(Loader.loadTexture("particles/smoke.png"), 8);
		

		ParticleSystem system = new ParticleSystem(particleTexture, 100, 10f, 0.3f, 4, 3f);
		system.randomizeRotation();
		system.setDirection(new Vector3f(0,0.001f,0), 0.00001f);
		system.setLifeError(0);
		system.setSpeedError(0);
		system.setScaleError(1f);
		return system;
		
	}
	
	private Terrain handleFlatTerrain() {
		TerrainTexture backgroundTexture = Loader.loadTerrainTexture("grass.png");
		TerrainTexture rTexture = Loader.loadTerrainTexture("mud.png");
		TerrainTexture gTexture = Loader.loadTerrainTexture("grassFlowers.png");
		TerrainTexture bTexture = Loader.loadTerrainTexture("path.png");
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		
		TerrainTexture blendMap = Loader.loadTerrainTexture("blendMap.png");
		
		Terrain terrain = new Terrain(-0.5f, -0.5f, texturePack, blendMap, "heightmap.png");
		data.flatTerrains.add(terrain);
		return terrain;
	}
	
}
