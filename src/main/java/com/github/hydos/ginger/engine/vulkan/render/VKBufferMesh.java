package com.github.hydos.ginger.engine.vulkan.render;

import com.github.hydos.ginger.engine.vulkan.model.VKVertex;
import com.github.hydos.ginger.engine.vulkan.model.VKModelLoader.VKMesh;

public class VKBufferMesh
{

	public long vertexBuffer;
	public long indexBuffer;
	public VKMesh vkMesh;
	public int[] indices;
	public VKVertex[] vertices;
	public long vertexBufferMemory;
	public long indexBufferMemory;
	
	
	
}
