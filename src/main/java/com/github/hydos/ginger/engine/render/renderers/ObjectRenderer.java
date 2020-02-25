package com.github.hydos.ginger.engine.render.renderers;

import java.util.*;

import org.lwjgl.opengl.*;

import com.github.halotroop.litecraft.types.block.BlockEntity;
import com.github.hydos.ginger.engine.api.GingerRegister;
import com.github.hydos.ginger.engine.elements.objects.RenderObject;
import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.math.Maths;
import com.github.hydos.ginger.engine.math.matrixes.Matrix4f;
import com.github.hydos.ginger.engine.render.*;
import com.github.hydos.ginger.engine.render.models.*;
import com.github.hydos.ginger.engine.render.shaders.StaticShader;
import com.github.hydos.ginger.engine.render.texture.ModelTexture;

public class ObjectRenderer extends Renderer
{
	private StaticShader shader;

	public ObjectRenderer(StaticShader shader, Matrix4f projectionMatrix)
	{
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public void prepare()
	{ GL11.glEnable(GL11.GL_DEPTH_TEST); }

	private void prepareInstance(RenderObject entity)
	{
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix);
	}

	private void prepareTexturedModel(TexturedModel model)
	{
		RawModel rawModel = model.getRawModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		ModelTexture texture = model.getTexture();
		if (texture.isTransparent())
		{
			MasterRenderer.disableCulling();
		}
		else
		{
			MasterRenderer.enableCulling();
		}
		shader.loadFakeLightingVariable(texture.isUseFakeLighting());
		shader.loadShine(texture.getShineDamper(), texture.getReflectivity());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getTextureID());
	}

	public void render(Map<TexturedModel, List<RenderObject>> entities)
	{
		for (TexturedModel model : entities.keySet())
		{
			prepareTexturedModel(model);
			List<RenderObject> batch = entities.get(model);
			for (RenderObject entity : batch)
			{
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
			unbindTexturedModel();
		}
	}

	private void unbindTexturedModel()
	{
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}

	public void render(List<BlockEntity> renderList)
	{ 
		prepare();
		shader.start();
		shader.loadSkyColour(Window.getColour());
		shader.loadViewMatrix(GingerRegister.getInstance().game.data.camera);
		for (RenderObject entity : renderList)
		{
			if(entity.getModel() != null) {
				TexturedModel model = entity.getModel();
				prepareTexturedModel(model);
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
				unbindTexturedModel();
			}
		}
		shader.stop();
	}

}
