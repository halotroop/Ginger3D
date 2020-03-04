package com.github.hydos.ginger.engine.vulkan.api;

import com.github.hydos.ginger.engine.vulkan.io.VkWindow;
import com.github.hydos.ginger.engine.vulkan.utils.VkUtils;

public class GingerVk
{
	
	public void start(String gameName) {
		System.out.println("Game " + gameName + " successfuly started in Vulkan mode.");
		VkUtils.createInstance();
		VkWindow.createSurface();
		VkUtils.createPhysicalDevice();
		VkUtils.createLogicalDevice();
	}
	
	
}
