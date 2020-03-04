package com.github.hydos.ginger.engine.vulkan.api;


import com.github.hydos.ginger.engine.vulkan.io.VKWindow;
import com.github.hydos.ginger.engine.vulkan.render.Swapchain;
import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;

public class GingerVK
{
	public void start(String gameName)
	{
		System.out.println("Game " + gameName + " successfuly started in Vulkan mode.");
		VKUtils.createInstance();
		VKWindow.createSurface();
		VKUtils.createPhysicalDevice();
		VKUtils.createLogicalDevice();
		Swapchain.createSwapChain();
	}
}
