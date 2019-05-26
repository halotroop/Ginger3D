package io.github.hydos.ginger;

import java.util.ArrayList;
import java.util.List;

import io.github.hydos.ginger.elements.Entity;
import io.github.hydos.ginger.elements.Light;
import io.github.hydos.ginger.elements.Player;
import io.github.hydos.ginger.elements.ThirdPersonCamera;
import io.github.hydos.ginger.font.FontType;
import io.github.hydos.ginger.font.GUIText;
import io.github.hydos.ginger.font.TextMaster;
import io.github.hydos.ginger.guis.GuiTexture;
import io.github.hydos.ginger.io.Window;
import io.github.hydos.ginger.mathEngine.vectors.Vector2f;
import io.github.hydos.ginger.mathEngine.vectors.Vector3f;
import io.github.hydos.ginger.mathEngine.vectors.Vector4f;
import io.github.hydos.ginger.obj.ModelLoader;
import io.github.hydos.ginger.obj.normals.NormalMappedObjLoader;
import io.github.hydos.ginger.renderEngine.MasterRenderer;
import io.github.hydos.ginger.renderEngine.models.TexturedModel;
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

	private List<Entity> normalMapEntities = new ArrayList<Entity>();
	
	
	public void main(String[] args) {
		
		
		Window.create(1200, 800, "Ginger Example", 60);
		
        Window.setBackgroundColour(0.2f, 0.2f, 0.8f);
		
        TextMaster.init();
        
        FontType font = new FontType(Loader.loadFontAtlas("calibri.png"), "calibri.fnt");
        
        GUIText text = new GUIText("hi this is some sample text", 1, font, new Vector2f(0,0), 1f, true);
        text.setColour(0, 1, 0);
        
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
		
		
		TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel.obj"), new ModelTexture("barrel.png"));
		barrelModel.getTexture().setNormalMap(new ModelTexture("modelNormals/barrelNormal.png").getTextureID());
		barrelModel.getTexture().setShineDamper(10f);
		barrelModel.getTexture().setReflectivity(0.5f);
		
		Entity barrel = new Entity(barrelModel, new Vector3f(1,terrain.getHeightOfTerrain(1, 1),1), 0, 0, 0, new Vector3f(0.25f,0.25f,0.25f));
		normalMapEntities.add(barrel);
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
					barrel.setPosition(terrainPoint);
				}
								
				dragon.increaseRotation(0,1,0);
				barrel.increaseRotation(0, 1, 0);
				masterRenderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, -1, 0, 100000));
				masterRenderer.renderGuis(guis);
				
				Window.swapBuffers();
			}
			
		}
		masterRenderer.cleanUp();
		TextMaster.cleanUp();
		Loader.cleanUp();
		System.exit(0);
		
	}
	
}
