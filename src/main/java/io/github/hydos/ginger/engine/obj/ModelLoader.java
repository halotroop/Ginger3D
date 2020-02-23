package io.github.hydos.ginger.engine.obj;

import io.github.hydos.ginger.engine.obj.shapes.StaticCube;
import io.github.hydos.ginger.engine.render.models.TexturedModel;
import io.github.hydos.ginger.engine.render.texture.ModelTexture;
import io.github.hydos.ginger.engine.utils.Loader;

public class ModelLoader {
	
	public static TexturedModel loadModel(String objPath, String texturePath) {
		Mesh data = OBJFileLoader.loadModel(objPath, texturePath);
		TexturedModel tm = new TexturedModel(Loader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(texturePath));
		return tm;
	}
	
	public static TexturedModel loadGenericCube(String cubeTexture) {
		Mesh data = StaticCube.getCube();
		TexturedModel tm = new TexturedModel(Loader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(cubeTexture));
		return tm;
	}
	
}
