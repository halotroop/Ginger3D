package io.github.hydos.ginger.main;

import io.github.hydos.ginger.engine.font.TextMaster;
import io.github.hydos.ginger.engine.obj.ModelLoader;
import io.github.hydos.ginger.engine.obj.normals.NormalMappedObjLoader;
import io.github.hydos.ginger.engine.renderEngine.models.RawModel;
import io.github.hydos.ginger.engine.renderEngine.models.TexturedModel;
import io.github.hydos.ginger.engine.renderEngine.texture.ModelTexture;

public class GingerMain {
	
	public static void init() {
        TextMaster.init();

	}
	
	public static TexturedModel createTexturedModel(String texturePath, String modelPath) {
		TexturedModel model = ModelLoader.loadModel(modelPath, texturePath);
		return model;
	}
	
	public static TexturedModel createTexturedModel(String texturePath, String modelPath, String normalMapPath) {
		RawModel model = NormalMappedObjLoader.loadOBJ(modelPath);
		TexturedModel texturedModel = new TexturedModel(model, new ModelTexture(texturePath));
		return texturedModel;
	}
	
	
	
	
	
}
