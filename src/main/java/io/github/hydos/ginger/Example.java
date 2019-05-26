package io.github.hydos.ginger;

import java.util.ArrayList;
import java.util.List;

import io.github.hydos.ginger.elements.Entity;
import io.github.hydos.ginger.elements.Light;
import io.github.hydos.ginger.elements.Player;
import io.github.hydos.ginger.elements.ThirdPersonCamera;
import io.github.hydos.ginger.guis.GuiTexture;
import io.github.hydos.ginger.io.Window;
import io.github.hydos.ginger.mathEngine.vectors.Vector2f;
import io.github.hydos.ginger.mathEngine.vectors.Vector3f;
import io.github.hydos.ginger.obj.ModelLoader;
import io.github.hydos.ginger.renderEngine.models.TexturedModel;
import io.github.hydos.ginger.renderEngine.renderers.MasterRenderer;
import io.github.hydos.ginger.renderEngine.texture.ModelTexture;
import io.github.hydos.ginger.renderEngine.tools.MousePicker;
import io.github.hydos.ginger.terrain.Terrain;
import io.github.hydos.ginger.terrain.TerrainTexture;
import io.github.hydos.ginger.terrain.TerrainTexturePack;
import io.github.hydos.ginger.utils.Loader;

public class Example {
	
	private MasterRenderer masterRenderer;
	
	private List<Terrain> terrains = new ArrayList<Terrain>();
	
	private List<GuiTexture> guis = new ArrayList<GuiTexture>();
	
	private List<Light> lights = new ArrayList<Light>();
		
	private List<Entity> entities = new ArrayList<Entity>();	
	
	
	public void main(String[] args) {
		
		
		Window.create(1200, 800, "Ginger Example", 60);
		
        Window.setBackgroundColour(0.2f, 0.2f, 0.8f);
		
        masterRenderer = new MasterRenderer();		

		TexturedModel tModel = ModelLoader.loadModel("stall.obj", "stallTexture.png");
		tModel.getTexture().setReflectivity(1f);
		tModel.getTexture().setShineDamper(7f);
		
		TexturedModel dragonMdl = ModelLoader.loadModel("dragon.obj", "stallTexture.png");
		dragonMdl.getTexture().setReflectivity(4f);
		dragonMdl.getTexture().setShineDamper(2f);
		

		
		Player entity = new Player(tModel, new Vector3f(0,0,-3),0,180f,0, new Vector3f(0.2f, 0.2f, 0.2f));
		Light sun = new Light(new Vector3f(0,-0.5f,0), new Vector3f(1, 1, 1), new Vector3f(0.1f, 0.1f, 0.1f));
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
		
		ThirdPersonCamera camera = new ThirdPersonCamera(new Vector3f(0,1,0), entity);
		
		GuiTexture guiTexture = new GuiTexture(new ModelTexture("guis/ginger.png").getTextureID(), new Vector2f(0.5f,0.5f), new Vector2f(0.25f,0.25f));
		guis.add(guiTexture);
		
		MousePicker picker = new MousePicker(camera, masterRenderer.getProjectionMatrix(), terrain);
		
		entities.add(entity);
		entities.add(dragon);
	
		
		terrains.add(terrain);
		
		while(!Window.closed()) {
			
			if(Window.isUpdating()) {
				Window.update();
				picker.update();
				camera.move();
				entity.move(terrain);
				Vector3f terrainPoint = picker.getCurrentTerrainPoint();
				if(terrainPoint!=null) {
					dragon.setPosition(terrainPoint);
				}
								
				dragon.increaseRotation(0,1,0);
				masterRenderer.renderScene(entities, terrains, lights, camera);
				masterRenderer.renderGuis(guis);
				
				Window.swapBuffers();
			}
			
		}
		masterRenderer.cleanUp();
		Loader.cleanUp();
		System.exit(0);
		
	}
	
}
