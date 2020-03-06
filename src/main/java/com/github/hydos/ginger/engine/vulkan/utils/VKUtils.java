package com.github.hydos.ginger.engine.vulkan.utils;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import com.github.hydos.ginger.VulkanExample;
import com.github.hydos.ginger.VulkanExample.QueueFamilyIndices;
import com.github.hydos.ginger.engine.vulkan.VKVariables;

public class VKUtils
{

	public static void createCommandPool() {

		try(MemoryStack stack = stackPush()) {

			QueueFamilyIndices queueFamilyIndices = VulkanExample.findQueueFamilies(VKVariables.physicalDevice);

			VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
			poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
			poolInfo.queueFamilyIndex(queueFamilyIndices.graphicsFamily);

			LongBuffer pCommandPool = stack.mallocLong(1);

			if (vkCreateCommandPool(VKVariables.device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create command pool");
			}

			VKVariables.commandPool = pCommandPool.get(0);
		}
	}
}
