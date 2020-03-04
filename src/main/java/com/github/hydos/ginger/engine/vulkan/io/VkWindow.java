package com.github.hydos.ginger.engine.vulkan.io;

import java.nio.LongBuffer;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;

import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.vulkan.VkConstants;
import com.github.hydos.ginger.engine.vulkan.utils.VkUtils;

/**
 * used for window related vulkan only things
 * @author hydos
 *
 */
public class VkWindow
{
	public static void createSurface()
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			LongBuffer pSurface = stack.longs(VK12.VK_NULL_HANDLE);
			
			int status = GLFWVulkan.glfwCreateWindowSurface(VkConstants.vulkanInstance, Window.getWindow(), null, pSurface);
			if(status != VK12.VK_SUCCESS)
			{
				throw new VulkanException("Failed to create vulkan surface for window reason: " + VkUtils.translateVulkanResult(status));
			}
			
			VkConstants.windowSurface = pSurface.get(0);
		}
	}
}
