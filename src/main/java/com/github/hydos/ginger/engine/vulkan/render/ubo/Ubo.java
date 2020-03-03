package com.github.hydos.ginger.engine.vulkan.render.ubo;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.*;

import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.vulkan.memory.VKMemory;
import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;
/**
 * A UBO is a uniform buffer object
 * i believe its used to give data from code to the shaders
 * @author hydos
 *
 */
public class Ubo {
	
	public UboDescriptor uboData;

	public Ubo(VkPhysicalDeviceMemoryProperties deviceMemoryProperties, VkDevice device) 
	{
		int err;
		// Create a new buffer
		VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
			.size(16 * 4)
			.usage(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
		LongBuffer pUniformDataVSBuffer = memAllocLong(1);
		err = vkCreateBuffer(device, bufferInfo, null, pUniformDataVSBuffer);
		long uniformDataVSBuffer = pUniformDataVSBuffer.get(0);
		memFree(pUniformDataVSBuffer);
		bufferInfo.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create UBO buffer: " + VKUtils.translateVulkanResult(err)); }
		// Get memory requirements including size, alignment and memory type
		VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		vkGetBufferMemoryRequirements(device, uniformDataVSBuffer, memReqs);
		long memSize = memReqs.size();
		int memoryTypeBits = memReqs.memoryTypeBits();
		memReqs.free();
		// Gets the appropriate memory type for this type of buffer allocation
		// Only memory types that are visible to the host
		IntBuffer pMemoryTypeIndex = memAllocInt(1);
		VKMemory.getMemoryType(deviceMemoryProperties, memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, pMemoryTypeIndex);
		int memoryTypeIndex = pMemoryTypeIndex.get(0);
		memFree(pMemoryTypeIndex);
		// Allocate memory for the uniform buffer
		LongBuffer pUniformDataVSMemory = memAllocLong(1);
		VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
			.allocationSize(memSize)
			.memoryTypeIndex(memoryTypeIndex);
		err = vkAllocateMemory(device, allocInfo, null, pUniformDataVSMemory);
		long uniformDataVSMemory = pUniformDataVSMemory.get(0);
		memFree(pUniformDataVSMemory);
		allocInfo.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to allocate UBO memory: " + VKUtils.translateVulkanResult(err)); }
		// Bind memory to buffer
		err = vkBindBufferMemory(device, uniformDataVSBuffer, uniformDataVSMemory, 0);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to bind UBO memory: " + VKUtils.translateVulkanResult(err)); }
		UboDescriptor ret = new UboDescriptor();
		ret.memory = uniformDataVSMemory;
		ret.buffer = uniformDataVSBuffer;
		ret.offset = 0L;
		ret.range = 16 * 4;
		this.uboData = ret;
	}
	
	public void updateUbo(VkDevice device, float angle)
	{
		Matrix4f m = new Matrix4f()
			.scale(1, -1, 1) // <- correcting viewport transformation (what Direct3D does, too)
			.perspective((float) Math.toRadians(45.0f), (float) Window.getWidth() / Window.getHeight(), 0.1f, 10.0f, true)
			.lookAt(0, 1, 3,
				0, 0, 0,
				0, 1, 0)
			.rotateY(angle);
		PointerBuffer pData = memAllocPointer(1);
		int err = vkMapMemory(device, uboData.memory, 0, 16 * 4, 0, pData);
		long data = pData.get(0);
		memFree(pData);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to map UBO memory: " + VKUtils.translateVulkanResult(err)); }
		ByteBuffer matrixBuffer = memByteBuffer(data, 16 * 4);
		m.get(matrixBuffer);
		vkUnmapMemory(device, uboData.memory);
	}
	
}
