package com.github.hydos.ginger.engine.vulkan.model;

import static org.lwjgl.system.MemoryUtil.*;

import java.nio.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.common.obj.Mesh;
import com.github.hydos.ginger.engine.vulkan.memory.VKMemory;
import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;

public class VKModelConverter
{
	
	public static VKVertices convertModel(Mesh mesh, VkPhysicalDeviceMemoryProperties deviceMemoryProperties, VkDevice device)
	{
		ByteBuffer vertexBuffer = memAlloc(mesh.getVertices().length * 4);
		FloatBuffer bufferVertices = vertexBuffer.asFloatBuffer();
		for(float vertex: mesh.getVertices()) {
			bufferVertices.put(vertex);
		}
		VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
		VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		int err;
		// Generate vertex buffer
		//  Setup
		VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
			.size(vertexBuffer.remaining())
			.usage(VK12.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		LongBuffer pBuffer = memAllocLong(1);
		err = VK12.vkCreateBuffer(device, bufInfo, null, pBuffer);
		long verticesBuf = pBuffer.get(0);
		memFree(pBuffer);
		bufInfo.free();
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create vertex buffer: " + VKUtils.translateVulkanResult(err)); }
		VK12.vkGetBufferMemoryRequirements(device, verticesBuf, memReqs);
		memAlloc.allocationSize(memReqs.size());
		IntBuffer memoryTypeIndex = memAllocInt(1);
		VKMemory.getMemoryType(deviceMemoryProperties, memReqs.memoryTypeBits(), VK12.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, memoryTypeIndex);
		memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		memFree(memoryTypeIndex);
		memReqs.free();
		LongBuffer pMemory = memAllocLong(1);
		err = VK12.vkAllocateMemory(device, memAlloc, null, pMemory);
		long verticesMem = pMemory.get(0);
		memFree(pMemory);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to allocate vertex memory: " + VKUtils.translateVulkanResult(err)); }
		PointerBuffer pData = memAllocPointer(1);
		err = VK12.vkMapMemory(device, verticesMem, 0, vertexBuffer.remaining(), 0, pData);
		memAlloc.free();
		long data = pData.get(0);
		memFree(pData);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to map vertex memory: " + VKUtils.translateVulkanResult(err)); }
		memCopy(memAddress(vertexBuffer), data, vertexBuffer.remaining());
		memFree(vertexBuffer);
		VK12.vkUnmapMemory(device, verticesMem);
		err = VK12.vkBindBufferMemory(device, verticesBuf, verticesMem, 0);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to bind memory to vertex buffer: " + VKUtils.translateVulkanResult(err)); }
		// Binding description
		VkVertexInputBindingDescription.Buffer bindingDescriptor = VkVertexInputBindingDescription.calloc(1)
			.binding(0) // <- we bind our vertex buffer to point 0
			.stride((3 + 3) * 4)
			.inputRate(VK12.VK_VERTEX_INPUT_RATE_VERTEX);
		// Attribute descriptions
		// Describes memory layout and shader attribute locations
		VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2);
		// Location 0 : Position
		attributeDescriptions.get(0)
			.binding(0) // <- binding point used in the VkVertexInputBindingDescription
			.location(0) // <- location in the shader's attribute layout (inside the shader source)
			.format(VK12.VK_FORMAT_R32G32B32_SFLOAT)
			.offset(0);
		// Location 1 : Color
		attributeDescriptions.get(1)
			.binding(0) // <- binding point used in the VkVertexInputBindingDescription
			.location(1) // <- location in the shader's attribute layout (inside the shader source)
			.format(VK12.VK_FORMAT_R32G32B32_SFLOAT)
			.offset(3 * 4);
		// Assign to vertex buffer
		VkPipelineVertexInputStateCreateInfo vi = VkPipelineVertexInputStateCreateInfo.calloc();
		vi.sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
		vi.pVertexBindingDescriptions(bindingDescriptor);
		vi.pVertexAttributeDescriptions(attributeDescriptions);
		VKVertices ret = new VKVertices();
		ret.createInfo = vi;
		ret.vkVerticiesBuffer = verticesBuf;
		return ret;
	}
	
}
