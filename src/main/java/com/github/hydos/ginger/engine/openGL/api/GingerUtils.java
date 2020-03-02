package com.github.hydos.ginger.engine.openGL.api;

import com.github.hydos.ginger.engine.common.obj.ModelLoader;
import com.github.hydos.ginger.engine.common.obj.normals.NormalMappedObjLoader;
import com.github.hydos.ginger.engine.openGL.font.TextMaster;
import com.github.hydos.ginger.engine.openGL.render.MasterRenderer;
import com.github.hydos.ginger.engine.openGL.render.models.*;
import com.github.hydos.ginger.engine.openGL.render.texture.ModelTexture;

public class GingerUtils
{
	public static TexturedModel createTexturedModel(String texturePath, String modelPath)
	{
		TexturedModel model = ModelLoader.loadModel(modelPath, texturePath);
		return model;
	}

	public static TexturedModel createTexturedModel(String texturePath, String modelPath, String normalMapPath)
	{
		RawModel model = NormalMappedObjLoader.loadOBJ(modelPath);
		TexturedModel texturedModel = new TexturedModel(model, new ModelTexture(texturePath));
		return texturedModel;
	}

	public static void init()
	{ TextMaster.init(); }

	public static void preRenderScene(MasterRenderer renderer)
	{}

	public static void update()
	{}
}
