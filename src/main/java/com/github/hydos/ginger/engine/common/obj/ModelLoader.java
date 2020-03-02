package com.github.hydos.ginger.engine.common.obj;

import com.github.hydos.ginger.engine.common.obj.shapes.StaticCube;
import com.github.hydos.ginger.engine.openGL.render.models.TexturedModel;
import com.github.hydos.ginger.engine.openGL.render.texture.ModelTexture;
import com.github.hydos.ginger.engine.openGL.utils.GlLoader;

public class ModelLoader
{
	public static TexturedModel loadGenericCube(String cubeTexture)
	{
		Mesh data = StaticCube.getCube();
		TexturedModel tm = new TexturedModel(GlLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(cubeTexture));
		return tm;
	}

	public static TexturedModel loadModel(String objPath, String texturePath)
	{
		Mesh data = OBJFileLoader.loadModel(objPath);
		return new TexturedModel(GlLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(texturePath));
	}
}
