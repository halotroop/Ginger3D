package com.github.hydos.ginger.engine.vulkan.api;

import com.github.hydos.ginger.engine.vulkan.io.VkWindow;
import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;

public class GingerVK
{
	public void start(String gameName)
	{
		System.out.println("Game " + gameName + " successfuly started in Vulkan mode.");
		VKUtils.createInstance();
		VkWindow.createSurface();
		VKUtils.createPhysicalDevice();
		VKUtils.createLogicalDevice();
	}
}
