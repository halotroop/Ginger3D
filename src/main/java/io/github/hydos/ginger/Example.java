package io.github.hydos.ginger;

import java.util.ArrayList;
import java.util.List;

import io.github.hydos.ginger.engine.elements.Entity;
import io.github.hydos.ginger.engine.elements.Light;
import io.github.hydos.ginger.engine.elements.Player;
import io.github.hydos.ginger.engine.elements.ThirdPersonCamera;
import io.github.hydos.ginger.engine.font.FontType;
import io.github.hydos.ginger.engine.font.GUIText;
import io.github.hydos.ginger.engine.font.TextMaster;
import io.github.hydos.ginger.engine.guis.GuiTexture;
import io.github.hydos.ginger.engine.io.Window;
import io.github.hydos.ginger.engine.mathEngine.vectors.Vector2f;
import io.github.hydos.ginger.engine.mathEngine.vectors.Vector3f;
import io.github.hydos.ginger.engine.mathEngine.vectors.Vector4f;
import io.github.hydos.ginger.engine.obj.ModelLoader;
import io.github.hydos.ginger.engine.obj.normals.NormalMappedObjLoader;
import io.github.hydos.ginger.engine.particle.ParticleMaster;
import io.github.hydos.ginger.engine.particle.ParticleSystem;
import io.github.hydos.ginger.engine.particle.ParticleTexture;
import io.github.hydos.ginger.engine.renderEngine.MasterRenderer;
import io.github.hydos.ginger.engine.renderEngine.models.TexturedModel;
import io.github.hydos.ginger.engine.renderEngine.texture.ModelTexture;
import io.github.hydos.ginger.engine.renderEngine.tools.MousePicker;
import io.github.hydos.ginger.engine.terrain.Terrain;
import io.github.hydos.ginger.engine.terrain.TerrainTexture;
import io.github.hydos.ginger.engine.terrain.TerrainTexturePack;
import io.github.hydos.ginger.engine.utils.Loader;

public class Example {
	
	private MasterRenderer masterRenderer;
	
	private List<Terrain> terrains = new ArrayList<Terrain>();
	
	private List<GuiTexture> guis = new ArrayList<GuiTexture>();
	
	private List<Light> lights = new ArrayList<Light>();
		
	private List<Entity> entities = new ArrayList<Entity>();

	private List<Entity> normalMapEntities = new ArrayList<Entity>();
	
	
	public void main(String[] args) {
		
		
		Window.create(1200, 800, "Ginger Example", 60);
		
        Window.setBackgroundColour(0.2f, 0.2f, 0.8f);
		
        TextMaster.init();
        
		TexturedModel tModel = ModelLoader.loadModel("stall.obj", "stallTexture.png");
		tModel.getTexture().setReflectivity(1f);
		tModel.getTexture().setShineDamper(7f);
		Player entity = new Player(tModel, new Vector3f(0,0,-3),0,180f,0, new Vector3f(0.2f, 0.2f, 0.2f));
		ThirdPersonCamera camera = new ThirdPersonCamera(new Vector3f(0,0.1f,0), entity);
        masterRenderer = new MasterRenderer(camera);		

        
        FontType font = new FontType(Loader.loadFontAtlas("candara.png"), "candara.fnt");
        
        GUIText text = new GUIText("hi, this is some sample text", 3, font, new Vector2f(0,0), 1f, true);
        text.setColour(0, 1, 0);
        text.setBorderWidth(0.7f);
        text.setBorderEdge(0.4f);
        text.setOffset(new Vector2f(0.003f, 0.003f));
                
        ParticleMaster.init(masterRenderer.getProjectionMatrix());
        
        

		
		TexturedModel dragonMdl = ModelLoader.loadModel("dragon.obj", "stallTexture.png");
		dragonMdl.getTexture().setReflectivity(4f);
		dragonMdl.getTexture().setShineDamper(2f);
		

		
		Light sun = new Light(new Vector3f(1000000,1500000,-1000000), new Vector3f(1.3f, 1.3f, 1.3f), new Vector3f(0f, 0f, 0f));
		lights.add(sun);
	
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
		entities.add(grassEntity);
		
		
		
		GuiTexture guiTexture = new GuiTexture(new ModelTexture("guis/ginger.png").getTextureID(), new Vector2f(0.5f,0.5f), new Vector2f(0.25f,0.25f));
		guis.add(guiTexture);
		
		MousePicker picker = new MousePicker(camera, masterRenderer.getProjectionMatrix(), terrain);
		
		
		TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel.obj"), new ModelTexture("barrel.png"));
		barrelModel.getTexture().setNormalMap(new ModelTexture("modelNormals/barrelNormal.png").getTextureID());
		barrelModel.getTexture().setShineDamper(10f);
		barrelModel.getTexture().setReflectivity(0.5f);
		
		Entity barrel = new Entity(barrelModel, new Vector3f(1,terrain.getHeightOfTerrain(1, 1),1), 0, 0, 0, new Vector3f(0.25f,0.25f,0.25f));
		normalMapEntities.add(barrel);
		entities.add(entity);
		entities.add(dragon);
	
		float colour = 0;
		terrains.add(terrain);
		
		GuiTexture shadowMap = new GuiTexture(masterRenderer.getShadowMapTexture(), new Vector2f(0.5f,0.5f), new Vector2f(0.5f,0.5f));
		guis.add(shadowMap);
		
		ParticleTexture particleTexture = new ParticleTexture(Loader.loadTexture("particles/smoke.png"), 8);
		
		ParticleSystem system = new ParticleSystem(particleTexture, 100, 5f, 0.3f, 4, 4f);
		system.randomizeRotation();
		system.setDirection(new Vector3f(0,0.001f,0), 0.00001f);
		system.setLifeError(0);
		system.setSpeedError(0);
		system.setScaleError(1f);
		
		while(!Window.closed()) {
			
			if(Window.isUpdating()) {
				Window.update();
				colour = colour + 0.001f;
				picker.update();
				ParticleMaster.update(camera);
				sun.setPosition(new Vector3f(entity.getPosition().x, entity.getPosition().y + 4, entity.getPosition().z));
				
				masterRenderer.renderShadowMap(entities, sun);
				
				camera.move();
				entity.move(terrain);
				text.setOutlineColour(new Vector3f(colour, colour /2, colour / 3));
				
				Vector3f terrainPoint = picker.getCurrentTerrainPoint();
				if(terrainPoint!=null) {
					barrel.setPosition(terrainPoint);
				}
				system.generateParticles(new Vector3f(0,-2,0));

				dragon.increaseRotation(0,1,0);
				barrel.increaseRotation(0, 1, 0);
				masterRenderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, -1, 0, 100000));
				ParticleMaster.renderParticles(camera);
				masterRenderer.renderGuis(guis);
				TextMaster.render();
				Window.swapBuffers();
			}
			
		}
		ParticleMaster.cleanUp();
		masterRenderer.cleanUp();
		TextMaster.cleanUp();
		Loader.cleanUp();
		System.exit(0);
		
	}
	
}
