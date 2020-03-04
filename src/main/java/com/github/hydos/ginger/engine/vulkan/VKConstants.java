package com.github.hydos.ginger.engine.vulkan;

import org.lwjgl.vulkan.*;

public class VKConstants
{
	
	public static final long UINT64_MAX = -1L;
	public static VkInstance vulkanInstance;
	public static VkPhysicalDevice physicalDevice;
	public static VkDevice device;
	public static VkQueue graphicsQueue;
	public static long windowSurface;
	public static VkQueue presentQueue;
	
}