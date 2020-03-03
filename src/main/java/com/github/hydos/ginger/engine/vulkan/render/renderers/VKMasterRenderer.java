package com.github.hydos.ginger.engine.vulkan.render.renderers;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import com.github.hydos.ginger.VulkanStarter;
import com.github.hydos.ginger.VulkanStarter.ColorAndDepthFormatAndSpace;
import com.github.hydos.ginger.VulkanStarter.DepthStencil;
import com.github.hydos.ginger.VulkanStarter.Swapchain;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.vulkan.memory.VKMemory;
import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;

public class VKMasterRenderer
{
	
	public static ColorAndDepthFormatAndSpace getColorFormatAndSpace(VkPhysicalDevice physicalDevice, long surface)
	{
		IntBuffer pQueueFamilyPropertyCount = memAllocInt(1);
		VK12.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
		int queueCount = pQueueFamilyPropertyCount.get(0);
		VkQueueFamilyProperties.Buffer queueProps = VkQueueFamilyProperties.calloc(queueCount);
		VK12.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, queueProps);
		memFree(pQueueFamilyPropertyCount);
		// Iterate over each queue to learn whether it supports presenting:
		IntBuffer supportsPresent = memAllocInt(queueCount);
		for (int i = 0; i < queueCount; i++)
		{
			supportsPresent.position(i);
			int err = vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, supportsPresent);
			if (err != VK12.VK_SUCCESS)
			{ throw new AssertionError("Failed to physical device surface support: " + VKUtils.translateVulkanResult(err)); }
		}
		// Search for a graphics and a present queue in the array of queue families, try to find one that supports both
		int graphicsQueueNodeIndex = Integer.MAX_VALUE;
		int presentQueueNodeIndex = Integer.MAX_VALUE;
		for (int i = 0; i < queueCount; i++)
		{
			if ((queueProps.get(i).queueFlags() & VK12.VK_QUEUE_GRAPHICS_BIT) != 0)
			{
				if (graphicsQueueNodeIndex == Integer.MAX_VALUE)
				{ graphicsQueueNodeIndex = i; }
				if (supportsPresent.get(i) == VK12.VK_TRUE)
				{
					graphicsQueueNodeIndex = i;
					presentQueueNodeIndex = i;
					break;
				}
			}
		}
		queueProps.free();
		if (presentQueueNodeIndex == Integer.MAX_VALUE)
		{
			// If there's no queue that supports both present and graphics try to find a separate present queue
			for (int i = 0; i < queueCount; ++i)
			{
				if (supportsPresent.get(i) == VK12.VK_TRUE)
				{
					presentQueueNodeIndex = i;
					break;
				}
			}
		}
		memFree(supportsPresent);
		// Generate error if could not find both a graphics and a present queue
		if (graphicsQueueNodeIndex == Integer.MAX_VALUE)
		{ throw new AssertionError("No graphics queue found"); }
		if (presentQueueNodeIndex == Integer.MAX_VALUE)
		{ throw new AssertionError("No presentation queue found"); }
		if (graphicsQueueNodeIndex != presentQueueNodeIndex)
		{ throw new AssertionError("Presentation queue != graphics queue"); }
		// Get list of supported formats
		IntBuffer pFormatCount = memAllocInt(1);
		int err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pFormatCount, null);
		int formatCount = pFormatCount.get(0);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to query number of physical device surface formats: " + VKUtils.translateVulkanResult(err)); }
		VkSurfaceFormatKHR.Buffer surfFormats = VkSurfaceFormatKHR.calloc(formatCount);
		err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pFormatCount, surfFormats);
		memFree(pFormatCount);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to query physical device surface formats: " + VKUtils.translateVulkanResult(err)); }
		int colorFormat;
		if (formatCount == 1 && surfFormats.get(0).format() == VK12.VK_FORMAT_UNDEFINED)
		{
			colorFormat = VK12.VK_FORMAT_B8G8R8A8_UNORM;
		}
		else
		{
			colorFormat = surfFormats.get(0).format();
		}
		int colorSpace = surfFormats.get(0).colorSpace();
		surfFormats.free();
		// Find suitable depth format
		IntBuffer pDepthFormat = memAllocInt(1).put(0, -1);
		VulkanStarter.getSupportedDepthFormat(physicalDevice, pDepthFormat);
		int depthFormat = pDepthFormat.get(0);
		ColorAndDepthFormatAndSpace ret = new ColorAndDepthFormatAndSpace();
		ret.colorFormat = colorFormat;
		ret.colorSpace = colorSpace;
		ret.depthFormat = depthFormat;
		return ret;
	}

	public static DepthStencil createDepthStencil(VkDevice device, VkPhysicalDeviceMemoryProperties physicalDeviceMemoryProperties, int depthFormat, VkCommandBuffer setupCmdBuffer)
	{
		VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
			.imageType(VK12.VK_IMAGE_TYPE_2D)
			.format(depthFormat)
			.mipLevels(1)
			.arrayLayers(1)
			.samples(VK12.VK_SAMPLE_COUNT_1_BIT)
			.tiling(VK12.VK_IMAGE_TILING_OPTIMAL)
			.usage(VK12.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK12.VK_IMAGE_USAGE_TRANSFER_SRC_BIT);
		imageCreateInfo.extent().width(Window.getWidth()).height(Window.getHeight()).depth(1);
		VkMemoryAllocateInfo mem_alloc = VkMemoryAllocateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
		VkImageViewCreateInfo depthStencilViewCreateInfo = VkImageViewCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
			.viewType(VK12.VK_IMAGE_VIEW_TYPE_2D)
			.format(depthFormat);
		depthStencilViewCreateInfo.subresourceRange()
			.aspectMask(VK12.VK_IMAGE_ASPECT_DEPTH_BIT | VK12.VK_IMAGE_ASPECT_STENCIL_BIT)
			.levelCount(1)
			.layerCount(1);
		VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		int err;
		LongBuffer pDepthStencilImage = memAllocLong(1);
		err = VK12.vkCreateImage(device, imageCreateInfo, null, pDepthStencilImage);
		long depthStencilImage = pDepthStencilImage.get(0);
		memFree(pDepthStencilImage);
		imageCreateInfo.free();
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create depth-stencil image: " + VKUtils.translateVulkanResult(err)); }
		VK12.vkGetImageMemoryRequirements(device, depthStencilImage, memReqs);
		mem_alloc.allocationSize(memReqs.size());
		IntBuffer pMemoryTypeIndex = memAllocInt(1);
		VKMemory.getMemoryType(physicalDeviceMemoryProperties, memReqs.memoryTypeBits(), VK12.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pMemoryTypeIndex);
		mem_alloc.memoryTypeIndex(pMemoryTypeIndex.get(0));
		memFree(pMemoryTypeIndex);
		LongBuffer pDepthStencilMem = memAllocLong(1);
		err = VK12.vkAllocateMemory(device, mem_alloc, null, pDepthStencilMem);
		long depthStencilMem = pDepthStencilMem.get(0);
		memFree(pDepthStencilMem);
		mem_alloc.free();
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create depth-stencil memory: " + VKUtils.translateVulkanResult(err)); }
		err = VK12.vkBindImageMemory(device, depthStencilImage, depthStencilMem, 0);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to bind depth-stencil image to memory: " + VKUtils.translateVulkanResult(err)); }
		depthStencilViewCreateInfo.image(depthStencilImage);
		LongBuffer pDepthStencilView = memAllocLong(1);
		err = VK12.vkCreateImageView(device, depthStencilViewCreateInfo, null, pDepthStencilView);
		long depthStencilView = pDepthStencilView.get(0);
		memFree(pDepthStencilView);
		depthStencilViewCreateInfo.free();
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create depth-stencil image view: " + VKUtils.translateVulkanResult(err)); }
		DepthStencil ret = new DepthStencil();
		ret.view = depthStencilView;
		return ret;
	}
	
	public static Swapchain createSwapChain(VkDevice device, VkPhysicalDevice physicalDevice, long surface, long oldSwapChain, VkCommandBuffer commandBuffer, int newWidth,
		int newHeight, int colorFormat, int colorSpace)
	{
		int err;
		// Get physical device surface properties and formats
		VkSurfaceCapabilitiesKHR surfCaps = VkSurfaceCapabilitiesKHR.calloc();
		err = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, surfCaps);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to get physical device surface capabilities: " + VKUtils.translateVulkanResult(err)); }
		IntBuffer pPresentModeCount = memAllocInt(1);
		err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, null);
		int presentModeCount = pPresentModeCount.get(0);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to get number of physical device surface presentation modes: " + VKUtils.translateVulkanResult(err)); }
		IntBuffer pPresentModes = memAllocInt(presentModeCount);
		err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, pPresentModes);
		memFree(pPresentModeCount);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to get physical device surface presentation modes: " + VKUtils.translateVulkanResult(err)); }
		// Try to use mailbox mode. Low latency and non-tearing
		int swapchainPresentMode = VK_PRESENT_MODE_FIFO_KHR;
		for (int i = 0; i < presentModeCount; i++)
		{
			if (pPresentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR)
			{
				swapchainPresentMode = VK_PRESENT_MODE_MAILBOX_KHR;
				break;
			}
			if ((swapchainPresentMode != VK_PRESENT_MODE_MAILBOX_KHR) && (pPresentModes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR))
			{ swapchainPresentMode = VK_PRESENT_MODE_IMMEDIATE_KHR; }
		}
		memFree(pPresentModes);
		// Determine the number of images
		int desiredNumberOfSwapchainImages = surfCaps.minImageCount() + 1;
		if ((surfCaps.maxImageCount() > 0) && (desiredNumberOfSwapchainImages > surfCaps.maxImageCount()))
		{ desiredNumberOfSwapchainImages = surfCaps.maxImageCount(); }
		VkExtent2D currentExtent = surfCaps.currentExtent();
		int currentWidth = currentExtent.width();
		int currentHeight = currentExtent.height();
		if (currentWidth != -1 && currentHeight != -1)
		{
			Window.width = currentWidth;
			Window.height = currentHeight;
		}
		else
		{
			Window.width = newWidth;
			Window.height = newHeight;
		}
		int preTransform;
		if ((surfCaps.supportedTransforms() & VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR) != 0)
		{
			preTransform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
		}
		else
		{
			preTransform = surfCaps.currentTransform();
		}
		surfCaps.free();
		VkSwapchainCreateInfoKHR swapchainCI = VkSwapchainCreateInfoKHR.calloc()
			.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
			.surface(surface)
			.minImageCount(desiredNumberOfSwapchainImages)
			.imageFormat(colorFormat)
			.imageColorSpace(colorSpace)
			.imageUsage(VK12.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
			.preTransform(preTransform)
			.imageArrayLayers(1)
			.imageSharingMode(VK12.VK_SHARING_MODE_EXCLUSIVE)
			.presentMode(swapchainPresentMode)
			.oldSwapchain(oldSwapChain)
			.clipped(true)
			.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
		swapchainCI.imageExtent()
			.width(Window.getWidth())
			.height(Window.getHeight());
		LongBuffer pSwapChain = memAllocLong(1);
		err = vkCreateSwapchainKHR(device, swapchainCI, null, pSwapChain);
		swapchainCI.free();
		long swapChain = pSwapChain.get(0);
		memFree(pSwapChain);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create swap chain: " + VKUtils.translateVulkanResult(err)); }
		// If we just re-created an existing swapchain, we should destroy the old swapchain at this point.
		// Note: destroying the swapchain also cleans up all its associated presentable images once the platform is done with them.
		if (oldSwapChain != VK12.VK_NULL_HANDLE)
		{ vkDestroySwapchainKHR(device, oldSwapChain, null); }
		IntBuffer pImageCount = memAllocInt(1);
		err = vkGetSwapchainImagesKHR(device, swapChain, pImageCount, null);
		int imageCount = pImageCount.get(0);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to get number of swapchain images: " + VKUtils.translateVulkanResult(err)); }
		LongBuffer pSwapchainImages = memAllocLong(imageCount);
		err = vkGetSwapchainImagesKHR(device, swapChain, pImageCount, pSwapchainImages);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to get swapchain images: " + VKUtils.translateVulkanResult(err)); }
		memFree(pImageCount);
		long[] images = new long[imageCount];
		long[] imageViews = new long[imageCount];
		LongBuffer pBufferView = memAllocLong(1);
		VkImageViewCreateInfo colorAttachmentView = VkImageViewCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
			.format(colorFormat)
			.viewType(VK12.VK_IMAGE_VIEW_TYPE_2D);
		colorAttachmentView.subresourceRange()
			.aspectMask(VK12.VK_IMAGE_ASPECT_COLOR_BIT)
			.levelCount(1)
			.layerCount(1);
		for (int i = 0; i < imageCount; i++)
		{
			images[i] = pSwapchainImages.get(i);
			colorAttachmentView.image(images[i]);
			err = VK12.vkCreateImageView(device, colorAttachmentView, null, pBufferView);
			imageViews[i] = pBufferView.get(0);
			if (err != VK12.VK_SUCCESS)
			{ throw new AssertionError("Failed to create image view: " + VKUtils.translateVulkanResult(err)); }
		}
		colorAttachmentView.free();
		memFree(pBufferView);
		memFree(pSwapchainImages);
		Swapchain ret = new Swapchain();
		ret.images = images;
		ret.imageViews = imageViews;
		ret.swapchainHandle = swapChain;
		return ret;
	}
	
}
