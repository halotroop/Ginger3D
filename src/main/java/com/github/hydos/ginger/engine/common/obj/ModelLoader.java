package com.github.hydos.ginger.engine.common.obj;

import com.github.hydos.ginger.engine.common.obj.shapes.StaticCube;
import com.github.hydos.ginger.engine.opengl.render.models.TexturedModel;
import com.github.hydos.ginger.engine.opengl.render.texture.ModelTexture;
import com.github.hydos.ginger.engine.opengl.utils.GLLoader;

public class ModelLoader
{
	public static TexturedModel loadGenericCube(String cubeTexture)
	{
		Mesh data = StaticCube.getCube();
		TexturedModel tm = new TexturedModel(GLLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(cubeTexture));
		return tm;
	}

	public static TexturedModel loadModel(String objPath, String texturePath)
	{
		Mesh data = OBJFileLoader.loadModel(objPath);
		return new TexturedModel(GLLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(texturePath));
	}
}
