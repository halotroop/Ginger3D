package com.github.hydos.ginger.engine.vulkan.render.renderers;

import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import com.github.hydos.ginger.VulkanExample;
import com.github.hydos.ginger.VulkanExample.VKVertex;
import com.github.hydos.ginger.engine.common.render.Renderer;
import com.github.hydos.ginger.engine.vulkan.VKVariables;
import com.github.hydos.ginger.engine.vulkan.misc.VKModelLoader.VKMesh;
import com.github.hydos.ginger.engine.vulkan.render.VKBufferMesh;

public class EntityRenderer extends Renderer
{
	
	public List<VKBufferMesh> entities;//TODO: batch rendering
	
	public EntityRenderer() {
		priority = 1;
		entities = new ArrayList<VKBufferMesh>();
	}
	
	public void processEntity(VKMesh mesh) {
		VKBufferMesh processedMesh = new VKBufferMesh();
		processedMesh.vkMesh = mesh;
		int vertexCount = mesh.positions.size();

		processedMesh.vertices = new VKVertex[vertexCount];

		Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

		for(int i = 0;i < vertexCount;i++) {
			processedMesh.vertices[i] = new VKVertex(
				mesh.positions.get(i),
				color,
				mesh.texCoords.get(i));
		}

		processedMesh.indices = new int[mesh.indices.size()];

		for(int i = 0;i < processedMesh.indices.length;i++) {
			processedMesh.indices[i] = mesh.indices.get(i);
		}
		
		processedMesh = VulkanExample.createVertexBuffer(processedMesh);
		processedMesh = VulkanExample.createIndexBuffer(processedMesh);
		entities.add(processedMesh);
	}
	
	@Override
	public void VKRender(MemoryStack stack, VkCommandBuffer commandBuffer, int index) 
	{
		
		for(VKBufferMesh entity : entities)
		{
	        LongBuffer vertexBuffers = stack.longs(entity.vertexBuffer);
	        LongBuffer offsets = stack.longs(0);
	        vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);

	        vkCmdBindIndexBuffer(commandBuffer, entity.indexBuffer, 0, VK_INDEX_TYPE_UINT32);

	        vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
	        	VKVariables.pipelineLayout,
	                0, stack.longs(
	                	VKVariables.descriptorSets.get(index)
	                	), 
	                null);

	        vkCmdDrawIndexed(commandBuffer, entity.vkMesh.indices.size(), 1, 0, 0, 0);
		}

	}
	
	
	
}
