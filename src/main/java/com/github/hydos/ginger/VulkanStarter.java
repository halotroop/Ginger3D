package com.github.hydos.ginger;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.vkDestroyDebugReportCallbackEXT;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_CLEAR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D16_UNORM_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D24_UNORM_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_STENCIL_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateRenderPass;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFormatProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;
import static org.lwjgl.vulkan.VK10.vkResetCommandPool;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.github.hydos.ginger.engine.common.info.RenderAPI;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.vulkan.VKConstants;
import com.github.hydos.ginger.engine.vulkan.memory.VKMemory;
import com.github.hydos.ginger.engine.vulkan.render.ubo.Ubo;
import com.github.hydos.ginger.engine.vulkan.render.ubo.UboDescriptor;
import com.github.hydos.ginger.engine.vulkan.shaders.Pipeline;
import com.github.hydos.ginger.engine.vulkan.utils.VKDeviceProperties;
import com.github.hydos.ginger.engine.vulkan.utils.VKLoader;
import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;
/**
 * 
 * @author hydos06
 * the non ARR vulkan test example
 *
 */
public class VulkanStarter
{

	private static boolean getSupportedDepthFormat(VkPhysicalDevice physicalDevice, IntBuffer depthFormat)
	{
		// Since all depth formats may be optional, we need to find a suitable depth format to use
		// Start with the highest precision packed format
		int[] depthFormats =
		{
			VK_FORMAT_D32_SFLOAT_S8_UINT,
			VK_FORMAT_D32_SFLOAT,
			VK_FORMAT_D24_UNORM_S8_UINT,
			VK_FORMAT_D16_UNORM_S8_UINT,
			VK_FORMAT_D16_UNORM
		};
		VkFormatProperties formatProps = VkFormatProperties.calloc();
		for (int format : depthFormats)
		{
			vkGetPhysicalDeviceFormatProperties(physicalDevice, format, formatProps);
			// Format must support depth stencil attachment for optimal tiling
			if ((formatProps.optimalTilingFeatures() & VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0)
			{
				depthFormat.put(0, format);
				return true;
			}
		}
		return false;
	}

	private static class ColorAndDepthFormatAndSpace
	{
		int colorFormat;
		int colorSpace;
		int depthFormat;
	}

	private static ColorAndDepthFormatAndSpace getColorFormatAndSpace(VkPhysicalDevice physicalDevice, long surface)
	{
		IntBuffer pQueueFamilyPropertyCount = memAllocInt(1);
		vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
		int queueCount = pQueueFamilyPropertyCount.get(0);
		VkQueueFamilyProperties.Buffer queueProps = VkQueueFamilyProperties.calloc(queueCount);
		vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, queueProps);
		memFree(pQueueFamilyPropertyCount);
		// Iterate over each queue to learn whether it supports presenting:
		IntBuffer supportsPresent = memAllocInt(queueCount);
		for (int i = 0; i < queueCount; i++)
		{
			supportsPresent.position(i);
			int err = vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, supportsPresent);
			if (err != VK_SUCCESS)
			{ throw new AssertionError("Failed to physical device surface support: " + VKUtils.translateVulkanResult(err)); }
		}
		// Search for a graphics and a present queue in the array of queue families, try to find one that supports both
		int graphicsQueueNodeIndex = Integer.MAX_VALUE;
		int presentQueueNodeIndex = Integer.MAX_VALUE;
		for (int i = 0; i < queueCount; i++)
		{
			if ((queueProps.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0)
			{
				if (graphicsQueueNodeIndex == Integer.MAX_VALUE)
				{ graphicsQueueNodeIndex = i; }
				if (supportsPresent.get(i) == VK_TRUE)
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
				if (supportsPresent.get(i) == VK_TRUE)
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
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to query number of physical device surface formats: " + VKUtils.translateVulkanResult(err)); }
		VkSurfaceFormatKHR.Buffer surfFormats = VkSurfaceFormatKHR.calloc(formatCount);
		err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pFormatCount, surfFormats);
		memFree(pFormatCount);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to query physical device surface formats: " + VKUtils.translateVulkanResult(err)); }
		int colorFormat;
		if (formatCount == 1 && surfFormats.get(0).format() == VK_FORMAT_UNDEFINED)
		{
			colorFormat = VK_FORMAT_B8G8R8A8_UNORM;
		}
		else
		{
			colorFormat = surfFormats.get(0).format();
		}
		int colorSpace = surfFormats.get(0).colorSpace();
		surfFormats.free();
		// Find suitable depth format
		IntBuffer pDepthFormat = memAllocInt(1).put(0, -1);
		getSupportedDepthFormat(physicalDevice, pDepthFormat);
		int depthFormat = pDepthFormat.get(0);
		ColorAndDepthFormatAndSpace ret = new ColorAndDepthFormatAndSpace();
		ret.colorFormat = colorFormat;
		ret.colorSpace = colorSpace;
		ret.depthFormat = depthFormat;
		return ret;
	}

	private static long createCommandPool(VkDevice device, int queueNodeIndex)
	{
		VkCommandPoolCreateInfo cmdPoolInfo = VkCommandPoolCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
			.queueFamilyIndex(queueNodeIndex)
			.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
		LongBuffer pCmdPool = memAllocLong(1);
		int err = vkCreateCommandPool(device, cmdPoolInfo, null, pCmdPool);
		long commandPool = pCmdPool.get(0);
		cmdPoolInfo.free();
		memFree(pCmdPool);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create command pool: " + VKUtils.translateVulkanResult(err)); }
		return commandPool;
	}

	private static VkQueue createDeviceQueue(VkDevice device, int queueFamilyIndex)
	{
		PointerBuffer pQueue = memAllocPointer(1);
		vkGetDeviceQueue(device, queueFamilyIndex, 0, pQueue);
		long queue = pQueue.get(0);
		memFree(pQueue);
		return new VkQueue(queue, device);
	}

	private static VkCommandBuffer createCommandBuffer(VkDevice device, long commandPool)
	{
		VkCommandBufferAllocateInfo cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
			.commandPool(commandPool)
			.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
			.commandBufferCount(1);
		PointerBuffer pCommandBuffer = memAllocPointer(1);
		int err = vkAllocateCommandBuffers(device, cmdBufAllocateInfo, pCommandBuffer);
		cmdBufAllocateInfo.free();
		long commandBuffer = pCommandBuffer.get(0);
		memFree(pCommandBuffer);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to allocate command buffer: " + VKUtils.translateVulkanResult(err)); }
		return new VkCommandBuffer(commandBuffer, device);
	}

	public static class Swapchain
	{
		long swapchainHandle;
		long[] images;
		long[] imageViews;
	}

	private static Swapchain createSwapChain(VkDevice device, VkPhysicalDevice physicalDevice, long surface, long oldSwapChain, VkCommandBuffer commandBuffer, int newWidth,
		int newHeight, int colorFormat, int colorSpace)
	{
		int err;
		// Get physical device surface properties and formats
		VkSurfaceCapabilitiesKHR surfCaps = VkSurfaceCapabilitiesKHR.calloc();
		err = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, surfCaps);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to get physical device surface capabilities: " + VKUtils.translateVulkanResult(err)); }
		IntBuffer pPresentModeCount = memAllocInt(1);
		err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, null);
		int presentModeCount = pPresentModeCount.get(0);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to get number of physical device surface presentation modes: " + VKUtils.translateVulkanResult(err)); }
		IntBuffer pPresentModes = memAllocInt(presentModeCount);
		err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, pPresentModes);
		memFree(pPresentModeCount);
		if (err != VK_SUCCESS)
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
			.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
			.preTransform(preTransform)
			.imageArrayLayers(1)
			.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
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
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create swap chain: " + VKUtils.translateVulkanResult(err)); }
		// If we just re-created an existing swapchain, we should destroy the old swapchain at this point.
		// Note: destroying the swapchain also cleans up all its associated presentable images once the platform is done with them.
		if (oldSwapChain != VK_NULL_HANDLE)
		{ vkDestroySwapchainKHR(device, oldSwapChain, null); }
		IntBuffer pImageCount = memAllocInt(1);
		err = vkGetSwapchainImagesKHR(device, swapChain, pImageCount, null);
		int imageCount = pImageCount.get(0);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to get number of swapchain images: " + VKUtils.translateVulkanResult(err)); }
		LongBuffer pSwapchainImages = memAllocLong(imageCount);
		err = vkGetSwapchainImagesKHR(device, swapChain, pImageCount, pSwapchainImages);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to get swapchain images: " + VKUtils.translateVulkanResult(err)); }
		memFree(pImageCount);
		long[] images = new long[imageCount];
		long[] imageViews = new long[imageCount];
		LongBuffer pBufferView = memAllocLong(1);
		VkImageViewCreateInfo colorAttachmentView = VkImageViewCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
			.format(colorFormat)
			.viewType(VK_IMAGE_VIEW_TYPE_2D);
		colorAttachmentView.subresourceRange()
			.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
			.levelCount(1)
			.layerCount(1);
		for (int i = 0; i < imageCount; i++)
		{
			images[i] = pSwapchainImages.get(i);
			colorAttachmentView.image(images[i]);
			err = vkCreateImageView(device, colorAttachmentView, null, pBufferView);
			imageViews[i] = pBufferView.get(0);
			if (err != VK_SUCCESS)
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

	private static class DepthStencil
	{
		long view;
	}

	private static DepthStencil createDepthStencil(VkDevice device, VkPhysicalDeviceMemoryProperties physicalDeviceMemoryProperties, int depthFormat, VkCommandBuffer setupCmdBuffer)
	{
		VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
			.imageType(VK_IMAGE_TYPE_2D)
			.format(depthFormat)
			.mipLevels(1)
			.arrayLayers(1)
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.tiling(VK_IMAGE_TILING_OPTIMAL)
			.usage(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT);
		imageCreateInfo.extent().width(Window.getWidth()).height(Window.getHeight()).depth(1);
		VkMemoryAllocateInfo mem_alloc = VkMemoryAllocateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
		VkImageViewCreateInfo depthStencilViewCreateInfo = VkImageViewCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
			.viewType(VK_IMAGE_VIEW_TYPE_2D)
			.format(depthFormat);
		depthStencilViewCreateInfo.subresourceRange()
			.aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT | VK_IMAGE_ASPECT_STENCIL_BIT)
			.levelCount(1)
			.layerCount(1);
		VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		int err;
		LongBuffer pDepthStencilImage = memAllocLong(1);
		err = vkCreateImage(device, imageCreateInfo, null, pDepthStencilImage);
		long depthStencilImage = pDepthStencilImage.get(0);
		memFree(pDepthStencilImage);
		imageCreateInfo.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create depth-stencil image: " + VKUtils.translateVulkanResult(err)); }
		vkGetImageMemoryRequirements(device, depthStencilImage, memReqs);
		mem_alloc.allocationSize(memReqs.size());
		IntBuffer pMemoryTypeIndex = memAllocInt(1);
		VKMemory.getMemoryType(physicalDeviceMemoryProperties, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pMemoryTypeIndex);
		mem_alloc.memoryTypeIndex(pMemoryTypeIndex.get(0));
		memFree(pMemoryTypeIndex);
		LongBuffer pDepthStencilMem = memAllocLong(1);
		err = vkAllocateMemory(device, mem_alloc, null, pDepthStencilMem);
		long depthStencilMem = pDepthStencilMem.get(0);
		memFree(pDepthStencilMem);
		mem_alloc.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create depth-stencil memory: " + VKUtils.translateVulkanResult(err)); }
		err = vkBindImageMemory(device, depthStencilImage, depthStencilMem, 0);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to bind depth-stencil image to memory: " + VKUtils.translateVulkanResult(err)); }
		depthStencilViewCreateInfo.image(depthStencilImage);
		LongBuffer pDepthStencilView = memAllocLong(1);
		err = vkCreateImageView(device, depthStencilViewCreateInfo, null, pDepthStencilView);
		long depthStencilView = pDepthStencilView.get(0);
		memFree(pDepthStencilView);
		depthStencilViewCreateInfo.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create depth-stencil image view: " + VKUtils.translateVulkanResult(err)); }
		DepthStencil ret = new DepthStencil();
		ret.view = depthStencilView;
		return ret;
	}

	private static long createRenderPass(VkDevice device, int colorFormat, int depthFormat)
	{
		VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2);
		attachments.get(0) // <- color attachment
			.format(colorFormat)
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
			.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
			.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
			.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
			.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
			.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
		attachments.get(1) // <- depth-stencil attachment
			.format(depthFormat)
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
			.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
			.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
			.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
			.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
			.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
		VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1)
			.attachment(0)
			.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		VkAttachmentReference depthReference = VkAttachmentReference.calloc()
			.attachment(1)
			.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1)
			.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
			.colorAttachmentCount(colorReference.remaining())
			.pColorAttachments(colorReference) // <- only color attachment
			.pDepthStencilAttachment(depthReference) // <- and depth-stencil
		;
		VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
			.pAttachments(attachments)
			.pSubpasses(subpass);
		LongBuffer pRenderPass = memAllocLong(1);
		int err = vkCreateRenderPass(device, renderPassInfo, null, pRenderPass);
		long renderPass = pRenderPass.get(0);
		memFree(pRenderPass);
		renderPassInfo.free();
		depthReference.free();
		colorReference.free();
		subpass.free();
		attachments.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create clear render pass: " + VKUtils.translateVulkanResult(err)); }
		return renderPass;
	}

	private static long[] createFramebuffers(VkDevice device, Swapchain swapchain, long renderPass, int width, int height, DepthStencil depthStencil)
	{
		LongBuffer attachments = memAllocLong(2);
		attachments.put(1, depthStencil.view);
		VkFramebufferCreateInfo fci = VkFramebufferCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
			.pAttachments(attachments)
			.height(height)
			.width(width)
			.layers(1)
			.renderPass(renderPass);
		// Create a framebuffer for each swapchain image
		long[] framebuffers = new long[swapchain.images.length];
		LongBuffer pFramebuffer = memAllocLong(1);
		for (int i = 0; i < swapchain.images.length; i++)
		{
			attachments.put(0, swapchain.imageViews[i]);
			int err = vkCreateFramebuffer(device, fci, null, pFramebuffer);
			long framebuffer = pFramebuffer.get(0);
			if (err != VK_SUCCESS)
			{ throw new AssertionError("Failed to create framebuffer: " + VKUtils.translateVulkanResult(err)); }
			framebuffers[i] = framebuffer;
		}
		memFree(attachments);
		memFree(pFramebuffer);
		fci.free();
		return framebuffers;
	}

	private static void submitCommandBuffer(VkQueue queue, VkCommandBuffer commandBuffer)
	{
		if (commandBuffer == null || commandBuffer.address() == NULL)
			return;
		VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
		PointerBuffer pCommandBuffers = memAllocPointer(1)
			.put(commandBuffer)
			.flip();
		submitInfo.pCommandBuffers(pCommandBuffers);
		int err = vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE);
		memFree(pCommandBuffers);
		submitInfo.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to submit command buffer: " + VKUtils.translateVulkanResult(err)); }
	}

	private static class Vertices
	{
		long verticesBuf;
		VkPipelineVertexInputStateCreateInfo createInfo;
	}

	private static Vertices createVertices(VkPhysicalDeviceMemoryProperties deviceMemoryProperties, VkDevice device)
	{
		ByteBuffer vertexBuffer = memAlloc(2 * 3 * (3 + 3) * 4);
		FloatBuffer fb = vertexBuffer.asFloatBuffer();
		// first triangle
		fb.put(-0.5f).put(-0.5f).put(0.5f).put(1.0f).put(0.0f).put(0.0f);
		fb.put(0.5f).put(-0.5f).put(0.5f).put(0.0f).put(1.0f).put(0.0f);
		fb.put(0.0f).put(0.5f).put(0.5f).put(0.0f).put(0.0f).put(1.0f);
		// second triangle
		fb.put(0.5f).put(-0.5f).put(-0.5f).put(1.0f).put(1.0f).put(0.0f);
		fb.put(-0.5f).put(-0.5f).put(-0.5f).put(0.0f).put(1.0f).put(1.0f);
		fb.put(0.0f).put(0.5f).put(-0.5f).put(1.0f).put(0.0f).put(1.0f);
		VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
		VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		int err;
		// Generate vertex buffer
		//  Setup
		VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
			.size(vertexBuffer.remaining())
			.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		LongBuffer pBuffer = memAllocLong(1);
		err = vkCreateBuffer(device, bufInfo, null, pBuffer);
		long verticesBuf = pBuffer.get(0);
		memFree(pBuffer);
		bufInfo.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create vertex buffer: " + VKUtils.translateVulkanResult(err)); }
		vkGetBufferMemoryRequirements(device, verticesBuf, memReqs);
		memAlloc.allocationSize(memReqs.size());
		IntBuffer memoryTypeIndex = memAllocInt(1);
		VKMemory.getMemoryType(deviceMemoryProperties, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, memoryTypeIndex);
		memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		memFree(memoryTypeIndex);
		memReqs.free();
		LongBuffer pMemory = memAllocLong(1);
		err = vkAllocateMemory(device, memAlloc, null, pMemory);
		long verticesMem = pMemory.get(0);
		memFree(pMemory);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to allocate vertex memory: " + VKUtils.translateVulkanResult(err)); }
		PointerBuffer pData = memAllocPointer(1);
		err = vkMapMemory(device, verticesMem, 0, vertexBuffer.remaining(), 0, pData);
		memAlloc.free();
		long data = pData.get(0);
		memFree(pData);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to map vertex memory: " + VKUtils.translateVulkanResult(err)); }
		memCopy(memAddress(vertexBuffer), data, vertexBuffer.remaining());
		memFree(vertexBuffer);
		vkUnmapMemory(device, verticesMem);
		err = vkBindBufferMemory(device, verticesBuf, verticesMem, 0);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to bind memory to vertex buffer: " + VKUtils.translateVulkanResult(err)); }
		// Binding description
		VkVertexInputBindingDescription.Buffer bindingDescriptor = VkVertexInputBindingDescription.calloc(1)
			.binding(0) // <- we bind our vertex buffer to point 0
			.stride((3 + 3) * 4)
			.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
		// Attribute descriptions
		// Describes memory layout and shader attribute locations
		VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2);
		// Location 0 : Position
		attributeDescriptions.get(0)
			.binding(0) // <- binding point used in the VkVertexInputBindingDescription
			.location(0) // <- location in the shader's attribute layout (inside the shader source)
			.format(VK_FORMAT_R32G32B32_SFLOAT)
			.offset(0);
		// Location 1 : Color
		attributeDescriptions.get(1)
			.binding(0) // <- binding point used in the VkVertexInputBindingDescription
			.location(1) // <- location in the shader's attribute layout (inside the shader source)
			.format(VK_FORMAT_R32G32B32_SFLOAT)
			.offset(3 * 4);
		// Assign to vertex buffer
		VkPipelineVertexInputStateCreateInfo vi = VkPipelineVertexInputStateCreateInfo.calloc();
		vi.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
		vi.pVertexBindingDescriptions(bindingDescriptor);
		vi.pVertexAttributeDescriptions(attributeDescriptions);
		Vertices ret = new Vertices();
		ret.createInfo = vi;
		ret.verticesBuf = verticesBuf;
		return ret;
	}

	private static long createDescriptorPool(VkDevice device)
	{
		// We need to tell the API the number of max. requested descriptors per type
		VkDescriptorPoolSize.Buffer typeCounts = VkDescriptorPoolSize.calloc(1)
			// This example only uses one descriptor type (uniform buffer) and only
			// requests one descriptor of this type
			.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1);
		// For additional types you need to add new entries in the type count list
		// E.g. for two combined image samplers :
		// typeCounts[1].type = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
		// typeCounts[1].descriptorCount = 2;
		// Create the global descriptor pool
		// All descriptors used in this example are allocated from this pool
		VkDescriptorPoolCreateInfo descriptorPoolInfo = VkDescriptorPoolCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
			.pPoolSizes(typeCounts)
			// Set the max. number of sets that can be requested
			// Requesting descriptors beyond maxSets will result in an error
			.maxSets(1);
		LongBuffer pDescriptorPool = memAllocLong(1);
		int err = vkCreateDescriptorPool(device, descriptorPoolInfo, null, pDescriptorPool);
		long descriptorPool = pDescriptorPool.get(0);
		memFree(pDescriptorPool);
		descriptorPoolInfo.free();
		typeCounts.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create descriptor pool: " + VKUtils.translateVulkanResult(err)); }
		return descriptorPool;
	}

	private static long createDescriptorSet(VkDevice device, long descriptorPool, long descriptorSetLayout, UboDescriptor uniformDataVSDescriptor)
	{
		LongBuffer pDescriptorSetLayout = memAllocLong(1);
		pDescriptorSetLayout.put(0, descriptorSetLayout);
		VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
			.descriptorPool(descriptorPool)
			.pSetLayouts(pDescriptorSetLayout);
		LongBuffer pDescriptorSet = memAllocLong(1);
		int err = vkAllocateDescriptorSets(device, allocInfo, pDescriptorSet);
		long descriptorSet = pDescriptorSet.get(0);
		memFree(pDescriptorSet);
		allocInfo.free();
		memFree(pDescriptorSetLayout);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create descriptor set: " + VKUtils.translateVulkanResult(err)); }
		// Update descriptor sets determining the shader binding points
		// For every binding point used in a shader there needs to be one
		// descriptor set matching that binding point
		VkDescriptorBufferInfo.Buffer descriptor = VkDescriptorBufferInfo.calloc(1)
			.buffer(uniformDataVSDescriptor.buffer)
			.range(uniformDataVSDescriptor.range)
			.offset(uniformDataVSDescriptor.offset);
		// Binding 0 : Uniform buffer
		VkWriteDescriptorSet.Buffer writeDescriptorSet = VkWriteDescriptorSet.calloc(1)
			.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
			.dstSet(descriptorSet)
			.descriptorCount(1)
			.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.pBufferInfo(descriptor)
			.dstBinding(0); // <- Binds this uniform buffer to binding point 0
		vkUpdateDescriptorSets(device, writeDescriptorSet, null);
		writeDescriptorSet.free();
		descriptor.free();
		return descriptorSet;
	}

	private static long createDescriptorSetLayout(VkDevice device)
	{
		int err;
		// One binding for a UBO used in a vertex shader
		VkDescriptorSetLayoutBinding.Buffer layoutBinding = VkDescriptorSetLayoutBinding.calloc(1)
			.binding(0) // <- Binding 0 : Uniform buffer (Vertex shader)
			.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1)
			.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
		// Build a create-info struct to create the descriptor set layout
		VkDescriptorSetLayoutCreateInfo descriptorLayout = VkDescriptorSetLayoutCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
			.pBindings(layoutBinding);
		LongBuffer pDescriptorSetLayout = memAllocLong(1);
		err = vkCreateDescriptorSetLayout(device, descriptorLayout, null, pDescriptorSetLayout);
		long descriptorSetLayout = pDescriptorSetLayout.get(0);
		memFree(pDescriptorSetLayout);
		descriptorLayout.free();
		layoutBinding.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create descriptor set layout: " + VKUtils.translateVulkanResult(err)); }
		return descriptorSetLayout;
	}

	/*
	 * All resources that must be reallocated on window resize.
	 */
	private static Swapchain swapchain;
	private static long[] framebuffers;
	private static VkCommandBuffer[] renderCommandBuffers;
	private static DepthStencil depthStencil;

	public static void main(String[] args) throws IOException
	{
		Window.create(1200, 600, "Vulkan Ginger3D", 60, RenderAPI.Vulkan);
		/* Look for instance extensions */
		PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
		if (requiredExtensions == null)
		{ throw new AssertionError("Failed to find list of required Vulkan extensions"); }
		// Create the Vulkan instance
		final VkInstance instance = VKLoader.createInstance(requiredExtensions);
		VKUtils.setupVulkanDebugCallback();
		final long debugCallbackHandle = VKUtils.startVulkanDebugging(instance, VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT, VKConstants.debugCallback);
		final VkPhysicalDevice physicalDevice = VKDeviceProperties.getFirstPhysicalDevice(instance);
		final VKDeviceProperties deviceAndGraphicsQueueFamily = VKDeviceProperties.initDeviceProperties(physicalDevice);
		final VkDevice device = deviceAndGraphicsQueueFamily.device;
		int queueFamilyIndex = deviceAndGraphicsQueueFamily.queueFamilyIndex;
		final VkPhysicalDeviceMemoryProperties memoryProperties = deviceAndGraphicsQueueFamily.memoryProperties;
		GLFWKeyCallback keyCallback;
		glfwSetKeyCallback(Window.getWindow(), keyCallback = new GLFWKeyCallback()
		{
			public void invoke(long window, int key, int scancode, int action, int mods)
			{
				if (action != GLFW_RELEASE)
					return;
				if (key == GLFW_KEY_ESCAPE)
					glfwSetWindowShouldClose(window, true);
			}
		});
		LongBuffer pSurface = memAllocLong(1);
		int err = glfwCreateWindowSurface(instance, Window.getWindow(), null, pSurface);
		final long surface = pSurface.get(0);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create surface: " + VKUtils.translateVulkanResult(err)); }
		// Create static Vulkan resources
		final ColorAndDepthFormatAndSpace colorAndDepthFormatAndSpace = getColorFormatAndSpace(physicalDevice, surface);
		final long commandPool = createCommandPool(device, queueFamilyIndex);
		final VkCommandBuffer setupCommandBuffer = createCommandBuffer(device, commandPool);
		final VkQueue queue = createDeviceQueue(device, queueFamilyIndex);
		final long renderPass = createRenderPass(device, colorAndDepthFormatAndSpace.colorFormat, colorAndDepthFormatAndSpace.depthFormat);
		final long renderCommandPool = createCommandPool(device, queueFamilyIndex);
		Vertices vertices = createVertices(memoryProperties, device);
		Ubo ubo = new Ubo(memoryProperties, device);
		final long descriptorPool = createDescriptorPool(device);
		final long descriptorSetLayout = createDescriptorSetLayout(device);
		final long descriptorSet = createDescriptorSet(device, descriptorPool, descriptorSetLayout, ubo.uboData);
		final Pipeline pipeline = Pipeline.createPipeline(device, renderPass, vertices.createInfo, descriptorSetLayout);
		final class SwapchainRecreator
		{
			boolean mustRecreate = true;

			void recreate()
			{
				// Begin the setup command buffer (the one we will use for swapchain/framebuffer creation)
				VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
				int err = vkBeginCommandBuffer(setupCommandBuffer, cmdBufInfo);
				cmdBufInfo.free();
				if (err != VK_SUCCESS)
				{ throw new AssertionError("Failed to begin setup command buffer: " + VKUtils.translateVulkanResult(err)); }
				long oldChain = swapchain != null ? swapchain.swapchainHandle : VK_NULL_HANDLE;
				// Create the swapchain (this will also add a memory barrier to initialize the framebuffer images)
				swapchain = createSwapChain(device, physicalDevice, surface, oldChain, setupCommandBuffer,
					Window.getWidth(), Window.getHeight(), colorAndDepthFormatAndSpace.colorFormat, colorAndDepthFormatAndSpace.colorSpace);
				// Create depth-stencil image
				depthStencil = createDepthStencil(device, memoryProperties, colorAndDepthFormatAndSpace.depthFormat, setupCommandBuffer);
				err = vkEndCommandBuffer(setupCommandBuffer);
				if (err != VK_SUCCESS)
				{ throw new AssertionError("Failed to end setup command buffer: " + VKUtils.translateVulkanResult(err)); }
				submitCommandBuffer(queue, setupCommandBuffer);
				vkQueueWaitIdle(queue);
				if (framebuffers != null)
				{ for (int i = 0; i < framebuffers.length; i++)
					vkDestroyFramebuffer(device, framebuffers[i], null); }
				framebuffers = createFramebuffers(device, swapchain, renderPass, Window.getWidth(), Window.getHeight(), depthStencil);
				// Create render command buffers
				if (renderCommandBuffers != null)
				{ vkResetCommandPool(device, renderCommandPool, VKUtils.VK_FLAGS_NONE); }
				renderCommandBuffers = VKUtils.initRenderCommandBuffers(device, renderCommandPool, framebuffers, renderPass, Window.getWidth(), Window.getHeight(), pipeline, descriptorSet,
					vertices.verticesBuf);
				mustRecreate = false;
			}
		}
		final SwapchainRecreator swapchainRecreator = new SwapchainRecreator();
		// Handle canvas resize
		GLFWFramebufferSizeCallback framebufferSizeCallback = new GLFWFramebufferSizeCallback()
		{
			public void invoke(long window, int width, int height)
			{
				if (width <= 0 || height <= 0)
					return;
				swapchainRecreator.mustRecreate = true;
			}
		};
		glfwSetFramebufferSizeCallback(Window.getWindow(), framebufferSizeCallback);
		glfwShowWindow(Window.getWindow());
		// Pre-allocate everything needed in the render loop
		IntBuffer pImageIndex = memAllocInt(1);
		int currentBuffer = 0;
		PointerBuffer pCommandBuffers = memAllocPointer(1);
		LongBuffer pSwapchains = memAllocLong(1);
		LongBuffer pImageAcquiredSemaphore = memAllocLong(1);
		LongBuffer pRenderCompleteSemaphore = memAllocLong(1);
		// Info struct to create a semaphore
		VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
		// Info struct to submit a command buffer which will wait on the semaphore
		IntBuffer pWaitDstStageMask = memAllocInt(1);
		pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
			.waitSemaphoreCount(pImageAcquiredSemaphore.remaining())
			.pWaitSemaphores(pImageAcquiredSemaphore)
			.pWaitDstStageMask(pWaitDstStageMask)
			.pCommandBuffers(pCommandBuffers)
			.pSignalSemaphores(pRenderCompleteSemaphore);
		// Info struct to present the current swapchain image to the display
		VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc()
			.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
			.pWaitSemaphores(pRenderCompleteSemaphore)
			.swapchainCount(pSwapchains.remaining())
			.pSwapchains(pSwapchains)
			.pImageIndices(pImageIndex)
			.pResults(null);
		// The render loop
		long lastTime = System.nanoTime();
		float time = 0.0f;
		while (!glfwWindowShouldClose(Window.getWindow()))
		{
			// Handle window messages. Resize events happen exactly here.
			// So it is safe to use the new swapchain images and framebuffers afterwards.
			glfwPollEvents();
			if (swapchainRecreator.mustRecreate)
				swapchainRecreator.recreate();
			// Create a semaphore to wait for the swapchain to acquire the next image
			err = vkCreateSemaphore(device, semaphoreCreateInfo, null, pImageAcquiredSemaphore);
			if (err != VK_SUCCESS)
			{ throw new AssertionError("Failed to create image acquired semaphore: " + VKUtils.translateVulkanResult(err)); }
			// Create a semaphore to wait for the render to complete, before presenting
			err = vkCreateSemaphore(device, semaphoreCreateInfo, null, pRenderCompleteSemaphore);
			if (err != VK_SUCCESS)
			{ throw new AssertionError("Failed to create render complete semaphore: " + VKUtils.translateVulkanResult(err)); }
			// Get next image from the swap chain (back/front buffer).
			// This will setup the imageAquiredSemaphore to be signalled when the operation is complete
			err = vkAcquireNextImageKHR(device, swapchain.swapchainHandle, VKConstants.MAX_UNSIGNED_INT, pImageAcquiredSemaphore.get(0), VK_NULL_HANDLE, pImageIndex);
			currentBuffer = pImageIndex.get(0);
			if (err != VK_SUCCESS)
			{ throw new AssertionError("Failed to acquire next swapchain image: " + VKUtils.translateVulkanResult(err)); }
			// Select the command buffer for the current framebuffer image/attachment
			pCommandBuffers.put(0, renderCommandBuffers[currentBuffer]);
			// Update UBO
			long thisTime = System.nanoTime();
			time += (thisTime - lastTime) / 1E9f;
			lastTime = thisTime;
			ubo.updateUbo(device, time);
			// Submit to the graphics queue
			err = vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE);
			if (err != VK_SUCCESS)
			{ throw new AssertionError("Failed to submit render queue: " + VKUtils.translateVulkanResult(err)); }
			// Present the current buffer to the swap chain
			// This will display the image
			pSwapchains.put(0, swapchain.swapchainHandle);
			err = vkQueuePresentKHR(queue, presentInfo);
			if (err != VK_SUCCESS)
			{ throw new AssertionError("Failed to present the swapchain image: " + VKUtils.translateVulkanResult(err)); }
			// Create and submit post present barrier
			vkQueueWaitIdle(queue);
			// Destroy this semaphore (we will create a new one in the next frame)
			vkDestroySemaphore(device, pImageAcquiredSemaphore.get(0), null);
			vkDestroySemaphore(device, pRenderCompleteSemaphore.get(0), null);
		}
		presentInfo.free();
		memFree(pWaitDstStageMask);
		submitInfo.free();
		memFree(pImageAcquiredSemaphore);
		memFree(pRenderCompleteSemaphore);
		semaphoreCreateInfo.free();
		memFree(pSwapchains);
		memFree(pCommandBuffers);
		vkDestroyDebugReportCallbackEXT(instance, debugCallbackHandle, null);
		framebufferSizeCallback.free();
		keyCallback.free();
		glfwDestroyWindow(Window.getWindow());
		glfwTerminate();
	}
}