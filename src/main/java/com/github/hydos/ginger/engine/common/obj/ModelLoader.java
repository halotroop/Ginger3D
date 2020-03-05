package com.github.hydos.ginger.engine.common.obj;

import org.joml.*;

import com.github.hydos.ginger.engine.common.obj.shapes.StaticCube;
import com.github.hydos.ginger.engine.opengl.render.models.GLTexturedModel;
import com.github.hydos.ginger.engine.opengl.render.texture.ModelTexture;
import com.github.hydos.ginger.engine.opengl.utils.GLLoader;

public class ModelLoader
{
	public static GLTexturedModel loadGenericCube(String cubeTexture)
	{
		Mesh data = StaticCube.getCube();
		GLTexturedModel tm = new GLTexturedModel(GLLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(cubeTexture));
		return tm;
	}
	
	public static Mesh getCubeMesh() {
		return StaticCube.getCube();
	}

	public static GLTexturedModel loadModel(String objPath, String texturePath)
	{
		Mesh data = OBJFileLoader.loadModel(objPath);
		return new GLTexturedModel(GLLoader.loadToVAO(data.getVertices(), data.getIndices(), data.getNormals(), data.getTextureCoords()), new ModelTexture(texturePath));
	}
	
	public static Mesh loadMesh(String meshPath) {
		Mesh data = OBJFileLoader.loadModel(meshPath);
		return data;
	}

	public static OptimisedMesh getCubeOptimisedMesh()
	{
		return optimiseModel(StaticCube.getCube()); 
	}

	private static OptimisedMesh optimiseModel(Mesh cube)
	{ 
		OptimisedMesh mesh = new OptimisedMesh();
		Vector3f position = new Vector3f();
		int index = 1;
		for(float f: cube.getVertices()) {
			if(index == 1) {
				position.x = f;
			}if(index == 2) {
				position.y = f;
			}if (index == 3) {
				position.z = f;
				mesh.positions.add(position);
				position = new Vector3f();
				index = 1;
			}
			if(index == 1 || index == 2) {
				index++;
			}
		}
		index = 1;
		Vector2f texCoord = new Vector2f();
		for(float f: cube.getTextureCoords()) {
			if(index == 1) {
				texCoord.x = f;
				index++;
			}else {
				texCoord.y = f;
				mesh.texCoords.add(texCoord);
				index = 1;
			}
		}
		for(float f: cube.getIndices()) {
			mesh.indices.add((int)f);
		}
		//optimised meshes don't have normals... yet
		return mesh; 
	}
}
