package io.github.hydos.ginger;

import io.github.hydos.ginger.engine.api.*;
import io.github.hydos.ginger.engine.cameras.Camera;
import io.github.hydos.ginger.engine.elements.buttons.TextureButton;
import io.github.hydos.ginger.engine.elements.objects.*;
import io.github.hydos.ginger.engine.font.*;
import io.github.hydos.ginger.engine.io.Window;
import io.github.hydos.ginger.engine.math.vectors.*;
import io.github.hydos.ginger.engine.obj.ModelLoader;
import io.github.hydos.ginger.engine.obj.normals.NormalMappedObjLoader;
import io.github.hydos.ginger.engine.particle.*;
import io.github.hydos.ginger.engine.render.MasterRenderer;
import io.github.hydos.ginger.engine.render.models.TexturedModel;
import io.github.hydos.ginger.engine.render.texture.ModelTexture;
import io.github.hydos.ginger.engine.terrain.*;
import io.github.hydos.ginger.engine.utils.Loader;
import io.github.hydos.ginger.main.GingerMain;
import io.github.hydos.ginger.main.settings.Constants;

public class Example extends Game{
	
	private Ginger ginger3D;
	
	public void main(String[] args) {
		//Render Player's constant variables
		Constants.movementSpeed = 0.000005f;
		Constants.turnSpeed = 0.00002f;
		Constants.gravity = -0.000000000005f;
		Constants.jumpPower = 0.000005f;
		
		Window.create(1200, 800, "Simple Ginger Example", 60);
		
		GingerMain.init();
		
        Window.setBackgroundColour(0.2f, 0.2f, 0.8f);
		
        
		TexturedModel tModel = ModelLoader.loadModel("stall.obj", "stallTexture.png");
		tModel.getTexture().setReflectivity(1f);
		tModel.getTexture().setShineDamper(7f);
		Player player = new Player(tModel, new Vector3f(0,0,-3),0,180f,0, new Vector3f(0.2f, 0.2f, 0.2f));
		Camera camera = new Camera(new Vector3f(0,0.1f,0), player);
		ginger3D = new Ginger();
		data = new GameData(player, camera);
		ginger3D.setup(new MasterRenderer(data.camera), data);

        
        FontType font = new FontType(Loader.loadFontAtlas("candara.png"), "candara.fnt");
        
        GUIText text = new GUIText("german", 3, font, new Vector2f(0,0), 1f, true);
        text.setColour(0, 1, 0);
                
        ParticleMaster.init(ginger3D.masterRenderer.getProjectionMatrix());
        
        

		
		TexturedModel dragonMdl = ModelLoader.loadModel("Zebra.obj", "stallTexture.png");
		dragonMdl.getTexture().setReflectivity(4f); 
		dragonMdl.getTexture().setShineDamper(2f);
		

		
		Light sun = new Light(new Vector3f(100,105,-100), new Vector3f(1.3f, 1.3f, 1.3f), new Vector3f(0.0001f, 0.0001f, 0.0001f));
		data.lights.add(sun);
	
		TexturedModel tgrass = ModelLoader.loadModel("grass.obj", "grass.png");
		tgrass.getTexture().setTransparency(true);
		tgrass.getTexture().useFakeLighting(true);
		
		TerrainTexture backgroundTexture = Loader.loadTerrainTexture("grass.png");
		TerrainTexture rTexture = Loader.loadTerrainTexture("mud.png");
		TerrainTexture gTexture = Loader.loadTerrainTexture("grassFlowers.png");
		TerrainTexture bTexture = Loader.loadTerrainTexture("path.png");
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		
		TerrainTexture blendMap = Loader.loadTerrainTexture("blendMap.png");
		
		Terrain terrain = new Terrain(-0.5f, -0.5f, texturePack, blendMap, "heightmap.png");
		
		Entity dragon = new Entity(dragonMdl, new Vector3f(3,terrain.getHeightOfTerrain(3, -3),-3),0,180f,0, new Vector3f(0.2f, 0.2f, 0.2f));
		
		Entity grassEntity = new Entity(tgrass, new Vector3f(-3,terrain.getHeightOfTerrain(-3, -3),-3),0,180f,0, new Vector3f(0.2f, 0.2f, 0.2f));
		data.entities.add(grassEntity);		
		
		TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel.obj"), new ModelTexture("barrel.png"));
		barrelModel.getTexture().setNormalMap(new ModelTexture("modelNormals/barrelNormal.png").getTextureID());
		barrelModel.getTexture().setShineDamper(10f);
		barrelModel.getTexture().setReflectivity(0.5f);
		
		Entity barrel = new Entity(barrelModel, new Vector3f(1,terrain.getHeightOfTerrain(1, 1),1), 0, 0, 0, new Vector3f(0.25f,0.25f,0.25f));
		data.normalMapEntities.add(barrel);
		data.entities.add(player);
		data.entities.add(dragon);
	
		data.flatTerrains.add(terrain);
		
		ParticleSystem system = setupParticles();
		
		TextureButton button = new TextureButton("/textures/guis/ginger.png", new Vector2f(0.8f, 0), new Vector2f(0.1f, 0.1f));
		button.show(data.guis);
		
		Window.lockMouse();
		
		while(!Window.closed()) {
			
			if(Window.isUpdating()) {
				ginger3D.update(data);
				
				ginger3D.masterRenderer.renderShadowMap(data.entities, sun);
				
				camera.move();
				player.move(terrain);
				
				system.generateParticles(new Vector3f(0,-2,0));
				
				ginger3D.render(data, this);
				
				dragon.increaseRotation(0,1,0);
				barrel.increaseRotation(0, 1, 0);
				
				button.update();
				if(button.isClicked()) {
					System.out.println("click");
					button.hide(data.guis);
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
	
}
