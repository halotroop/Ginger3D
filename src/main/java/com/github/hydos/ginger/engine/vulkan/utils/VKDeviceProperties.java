package com.github.hydos.ginger.engine.vulkan.utils;

import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import com.github.hydos.ginger.engine.vulkan.VKConstants;

public class VKDeviceProperties {
	public VkDevice device;
	public int queueFamilyIndex;
	public VkPhysicalDeviceMemoryProperties memoryProperties;
	
	public static VkPhysicalDevice getFirstPhysicalDevice(VkInstance instance)
	{
		IntBuffer pPhysicalDeviceCount = memAllocInt(1);
		int err = VK12.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, null);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to get number of physical devices: " + VKUtils.translateVulkanResult(err)); }
		PointerBuffer pPhysicalDevices = memAllocPointer(pPhysicalDeviceCount.get(0));
		err = VK12.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices);
		long physicalDevice = pPhysicalDevices.get(0);
		memFree(pPhysicalDeviceCount);
		memFree(pPhysicalDevices);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to get physical devices: " + VKUtils.translateVulkanResult(err)); }
		return new VkPhysicalDevice(physicalDevice, instance);
	}
	
	public static VKDeviceProperties initDeviceProperties(VkPhysicalDevice physicalDevice)
	{
		IntBuffer pQueueFamilyPropertyCount = memAllocInt(1);
		VK12.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
		int queueCount = pQueueFamilyPropertyCount.get(0);
		VkQueueFamilyProperties.Buffer queueProps = VkQueueFamilyProperties.calloc(queueCount);
		VK12.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, queueProps);
		memFree(pQueueFamilyPropertyCount);
		int graphicsQueueFamilyIndex;
		for (graphicsQueueFamilyIndex = 0; graphicsQueueFamilyIndex < queueCount; graphicsQueueFamilyIndex++)
		{ if ((queueProps.get(graphicsQueueFamilyIndex).queueFlags() & VK12.VK_QUEUE_GRAPHICS_BIT) != 0)
			break; }
		queueProps.free();
		FloatBuffer pQueuePriorities = memAllocFloat(1).put(0.0f);
		pQueuePriorities.flip();
		VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
			.sType(VK12.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
			.queueFamilyIndex(graphicsQueueFamilyIndex)
			.pQueuePriorities(pQueuePriorities);
		PointerBuffer extensions = memAllocPointer(1);
		ByteBuffer VK_KHR_SWAPCHAIN_EXTENSION = memUTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
		extensions.put(VK_KHR_SWAPCHAIN_EXTENSION);
		extensions.flip();
		PointerBuffer ppEnabledLayerNames = memAllocPointer(VKConstants.layers.length);
		for (int i = 0; VKConstants.debug && i < VKConstants.layers.length; i++)
			ppEnabledLayerNames.put(VKConstants.layers[i]);
		ppEnabledLayerNames.flip();
		VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
			.pQueueCreateInfos(queueCreateInfo)
			.ppEnabledExtensionNames(extensions)
			.ppEnabledLayerNames(ppEnabledLayerNames);
		PointerBuffer pDevice = memAllocPointer(1);
		int err = VK12.vkCreateDevice(physicalDevice, deviceCreateInfo, null, pDevice);
		long device = pDevice.get(0);
		memFree(pDevice);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create device: " + VKUtils.translateVulkanResult(err)); }
		VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
		VK12.vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);
		VKDeviceProperties ret = new VKDeviceProperties();
		ret.device = new VkDevice(device, physicalDevice, deviceCreateInfo);
		ret.queueFamilyIndex = graphicsQueueFamilyIndex;
		ret.memoryProperties = memoryProperties;
		deviceCreateInfo.free();
		memFree(ppEnabledLayerNames);
		memFree(VK_KHR_SWAPCHAIN_EXTENSION);
		memFree(extensions);
		memFree(pQueuePriorities);
		return ret;
	}
}
