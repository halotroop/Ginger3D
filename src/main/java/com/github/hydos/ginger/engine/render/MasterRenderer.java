package com.github.hydos.ginger.engine.render;

import java.lang.Math;
import java.util.*;

import org.joml.*;
import org.lwjgl.opengl.*;

import com.github.halotroop.litecraft.world.World;
import com.github.halotroop.litecraft.world.block.BlockRenderer;
import com.github.hydos.ginger.engine.cameras.Camera;
import com.github.hydos.ginger.engine.elements.GuiTexture;
import com.github.hydos.ginger.engine.elements.objects.*;
import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.render.models.TexturedModel;
import com.github.hydos.ginger.engine.render.renderers.*;
import com.github.hydos.ginger.engine.render.shaders.*;
import com.github.hydos.ginger.engine.shadow.ShadowMapMasterRenderer;

public class MasterRenderer
{
	public static final float FOV = 80f;
	public static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000f;
	public static void disableCulling()
	{ GL11.glDisable(GL11.GL_CULL_FACE); }
	
	public static void enableCulling()
	{
//		GL11.glEnable(GL11.GL_CULL_FACE);
//		GL11.glCullFace(GL11.GL_BACK);
	}
	
	public BlockRenderer blockRenderer;
	private StaticShader entityShader;
	public ObjectRenderer entityRenderer;
	private GuiShader guiShader;
	private GuiRenderer guiRenderer;
	private SkyboxRenderer skyboxRenderer;
	private NormalMappingRenderer normalRenderer;
	private Matrix4f projectionMatrix;
	private ShadowMapMasterRenderer shadowMapRenderer;

	private Map<TexturedModel, List<RenderObject>> entities = new HashMap<TexturedModel, List<RenderObject>>();

	private Map<TexturedModel, List<RenderObject>> normalMapEntities = new HashMap<TexturedModel, List<RenderObject>>();

	public MasterRenderer(Camera camera)
	{
		createProjectionMatrix();
		entityShader = new StaticShader();
		blockRenderer = new BlockRenderer(entityShader, projectionMatrix);
		entityRenderer = new ObjectRenderer(entityShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(projectionMatrix);
		guiShader = new GuiShader();
		guiRenderer = new GuiRenderer(guiShader);
		normalRenderer = new NormalMappingRenderer(projectionMatrix);
		shadowMapRenderer = new ShadowMapMasterRenderer(camera);
	}

	public void cleanUp()
	{
		entityShader.cleanUp();
		guiRenderer.cleanUp();
		shadowMapRenderer.cleanUp();
		normalRenderer.cleanUp();
	}

	private void createProjectionMatrix()
	{
		projectionMatrix = new Matrix4f();
		float aspectRatio = (float) Window.width / (float) Window.height;
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;
		projectionMatrix._m00(x_scale);
		projectionMatrix._m11(y_scale);
		projectionMatrix._m22 (-((FAR_PLANE + NEAR_PLANE) / frustum_length));
		projectionMatrix._m23(-1);
		projectionMatrix._m32(-((2 * NEAR_PLANE * FAR_PLANE) / frustum_length));
		projectionMatrix._m33(0);
	}

	public Matrix4f getProjectionMatrix()
	{ return this.projectionMatrix; }

	public int getShadowMapTexture()
	{ return shadowMapRenderer.getShadowMap(); }

	public void prepare()
	{
		GL13.glActiveTexture(GL13.GL_TEXTURE5);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowMapRenderer.getShadowMap());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
	}

	private void processEntity(RenderObject entity)
	{
		TexturedModel entityModel = entity.getModel();
		List<RenderObject> batch = entities.get(entityModel);
		if (batch != null)
		{
			batch.add(entity);
		}
		else
		{
			List<RenderObject> newBatch = new ArrayList<RenderObject>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}

	private void processEntityWithNormal(RenderObject entity)
	{
		TexturedModel entityModel = entity.getModel();
		List<RenderObject> batch = normalMapEntities.get(entityModel);
		if (batch != null)
		{
			batch.add(entity);
		}
		else
		{
			List<RenderObject> newBatch = new ArrayList<RenderObject>();
			newBatch.add(entity);
			normalMapEntities.put(entityModel, newBatch);
		}
	}

	private void renderEntities(List<RenderObject> entities, Camera camera, List<Light> lights)
	{
		for (RenderObject entity : entities)
		{ processEntity(entity); }
		entityRenderer.prepare();
		entityShader.start();
		entityShader.loadSkyColour(Window.getColour());
		entityShader.loadLights(lights);
		entityShader.loadViewMatrix(camera);
		entityRenderer.render(this.entities);
		entityShader.stop();
		this.entities.clear();
	}

	public void renderGui(GuiTexture guiTexture)
	{
		List<GuiTexture> texture = new ArrayList<GuiTexture>();
		texture.add(guiTexture);
		guiRenderer.render(texture);
	}

	public void renderGuis(List<GuiTexture> guis)
	{ guiRenderer.render(guis); }

	private void renderNormalEntities(List<RenderObject> normalEntities, List<Light> lights, Camera camera, Vector4f clipPlane)
	{
		for (RenderObject entity : normalEntities)
		{ processEntityWithNormal(entity); }
		normalRenderer.render(normalMapEntities, clipPlane, lights, camera);
	}

	public void renderScene(List<RenderObject> entities, List<RenderObject> normalEntities, List<Light> lights, Camera camera, Vector4f clipPlane, World world)
	{
		prepare();
		renderEntities(entities, camera, lights);
		world.render(blockRenderer);
		renderNormalEntities(normalEntities, lights, camera, clipPlane);
		skyboxRenderer.render(camera);
	}

	public void renderShadowMap(List<RenderObject> entityList, Light sun)
	{
		for (RenderObject entity : entityList)
		{ processEntity(entity); }
		shadowMapRenderer.render(entities, sun);
		entities.clear();
	}
}
