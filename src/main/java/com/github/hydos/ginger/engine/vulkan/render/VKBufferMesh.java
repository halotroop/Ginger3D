package com.github.hydos.ginger.engine.vulkan.render;

import static org.lwjgl.vulkan.VK10.*;

import com.github.hydos.ginger.engine.vulkan.VKVariables;
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
	
	public void cleanup() {
		vkDestroyBuffer(VKVariables.device, indexBuffer, null);
		vkFreeMemory(VKVariables.device, indexBufferMemory, null);

		vkDestroyBuffer(VKVariables.device, vertexBuffer, null);
		vkFreeMemory(VKVariables.device, vertexBufferMemory, null);
	}
	
}
