package com.github.hydos.ginger.engine.vulkan.utils;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

import com.github.hydos.ginger.VulkanExample;
import com.github.hydos.ginger.engine.vulkan.VKVariables;
import com.github.hydos.ginger.engine.vulkan.managers.CommandBufferManager;

public class VKBufferUtils
{

	public static void createBuffer(long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory) {

		try(MemoryStack stack = stackPush()) {

			VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack);
			bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
			bufferInfo.size(size);
			bufferInfo.usage(usage);
			bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

			if(vkCreateBuffer(VKVariables.device, bufferInfo, null, pBuffer) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create vertex buffer");
			}

			VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
			vkGetBufferMemoryRequirements(VKVariables.device, pBuffer.get(0), memRequirements);

			VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
			allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
			allocInfo.allocationSize(memRequirements.size());
			allocInfo.memoryTypeIndex(VulkanExample.findMemoryType(memRequirements.memoryTypeBits(), properties));

			if(vkAllocateMemory(VKVariables.device, allocInfo, null, pBufferMemory) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate vertex buffer memory");
			}

			vkBindBufferMemory(VKVariables.device, pBuffer.get(0), pBufferMemory.get(0), 0);
		}
	}

	public static void copyBuffer(long srcBuffer, long dstBuffer, long size) {

		try(MemoryStack stack = stackPush()) {

			VkCommandBuffer commandBuffer = CommandBufferManager.beginSingleTimeCommands();

			VkBufferCopy.Buffer copyRegion = VkBufferCopy.callocStack(1, stack);
			copyRegion.size(size);

			vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);

			CommandBufferManager.endSingleTimeCommands(commandBuffer);
		}
	}
	
}
