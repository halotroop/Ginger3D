package com.github.hydos.ginger.engine.common.obj;

import com.github.hydos.ginger.engine.common.obj.shapes.StaticCube;
import com.github.hydos.ginger.engine.opengl.render.models.GLTexturedModel;
import com.github.hydos.ginger.engine.opengl.render.texture.ModelTexture;
import com.github.hydos.ginger.engine.opengl.utils.GLLoader;

public class ModelLoader
{
	public static GLTexturedModel loadGenericCube(String cubeTexture)
	{
		Mesh data = StaticCube.getCube();
<<<<<<< Upstream, based on origin/liteCraft
		TexturedModel tm = new TexturedModel(GLLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(cubeTexture));
=======
		GLTexturedModel tm = new GLTexturedModel(GlLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(cubeTexture));
>>>>>>> 9a8fdc4 yEs
		return tm;
	}
	
	public static Mesh getCubeMesh() {
		return StaticCube.getCube();
	}

	public static GLTexturedModel loadModel(String objPath, String texturePath)
	{
		Mesh data = OBJFileLoader.loadModel(objPath);
<<<<<<< Upstream, based on origin/liteCraft
		return new TexturedModel(GLLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(texturePath));
=======
		return new GLTexturedModel(GlLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(texturePath));
>>>>>>> 9a8fdc4 yEs
	}
}
