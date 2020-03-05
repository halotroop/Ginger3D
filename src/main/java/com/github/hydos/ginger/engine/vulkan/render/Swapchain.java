package com.github.hydos.ginger.engine.vulkan.render;

import java.nio.*;
import java.util.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.vulkan.VKConstants;
import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;
import com.github.hydos.ginger.engine.vulkan.utils.VKUtils.QueueFamilyIndices;

/**
 * used for Vulkan FBO management as vulkan does not have a default FBO
 * @author hydos
 *
 */
public class Swapchain
{
	
	public static long swapChain;
	public static List<Long> swapChainImages;
	public static int swapChainImageFormat;
	public static VkExtent2D swapChainExtent;
	
	public static class SwapChainSupportDetails
	{
		public VkSurfaceCapabilitiesKHR capabilities;
		public VkSurfaceFormatKHR.Buffer formats;
		public IntBuffer presentModes;
	}
	
    private static SwapChainSupportDetails querySwapChainSupport(MemoryStack stack) {

        SwapChainSupportDetails details = new SwapChainSupportDetails();

        details.capabilities = VkSurfaceCapabilitiesKHR.mallocStack(stack);
        KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(VKConstants.physicalDevice, VKConstants.windowSurface, details.capabilities);

        IntBuffer count = stack.ints(0);

        KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(VKConstants.physicalDevice, VKConstants.windowSurface, count, null);

        if(count.get(0) != 0) {
            details.formats = VkSurfaceFormatKHR.mallocStack(count.get(0), stack);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(VKConstants.physicalDevice, VKConstants.windowSurface, count, details.formats);
        }

        KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(VKConstants.physicalDevice,VKConstants.windowSurface, count, null);

        if(count.get(0) != 0) {
            details.presentModes = stack.mallocInt(count.get(0));
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(VKConstants.physicalDevice, VKConstants.windowSurface, count, details.presentModes);
        }

        return details;
    }
    
    private static VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
        return availableFormats.stream()
                .filter(availableFormat -> availableFormat.format() == VK12.VK_FORMAT_B8G8R8_UNORM)
                .filter(availableFormat -> availableFormat.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
                .findAny()
                .orElse(availableFormats.get(0));
    }
    
    private static int chooseSwapPresentMode(IntBuffer availablePresentModes) {

        for(int i = 0;i < availablePresentModes.capacity();i++) {
            if(availablePresentModes.get(i) == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR) {
                return availablePresentModes.get(i);
            }
        }

        return KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
    }
   

    private static VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities) {

        if(capabilities.currentExtent().width() != VKConstants.UINT64_MAX) {
            return capabilities.currentExtent();
        }

        VkExtent2D actualExtent = VkExtent2D.mallocStack().set(Window.getWidth(), Window.getHeight());

        VkExtent2D minExtent = capabilities.minImageExtent();
        VkExtent2D maxExtent = capabilities.maxImageExtent();

        actualExtent.width(VKUtils.clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
        actualExtent.height(VKUtils.clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

        return actualExtent;
    }

	public static void createSwapChain()
	{
        try(MemoryStack stack = MemoryStack.stackPush()) {
            SwapChainSupportDetails swapChainSupport = querySwapChainSupport(stack);

            VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
            int presentMode = chooseSwapPresentMode(swapChainSupport.presentModes);
            VkExtent2D extent = chooseSwapExtent(swapChainSupport.capabilities);

            IntBuffer imageCount = stack.ints(swapChainSupport.capabilities.minImageCount() + 1);

            if(swapChainSupport.capabilities.maxImageCount() > 0 && imageCount.get(0) > swapChainSupport.capabilities.maxImageCount()) {
                imageCount.put(0, swapChainSupport.capabilities.maxImageCount());
            }

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc()
            	.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
            	.minImageCount(imageCount.get(0))
            	.imageFormat(surfaceFormat.format())
            	.imageColorSpace(surfaceFormat.colorSpace())
            	.imageExtent(extent)
            	.imageArrayLayers(1)
            	.imageUsage(VK12.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            QueueFamilyIndices indices = VKUtils.findQueueFamilies();

            if(!indices.graphicsFamily.equals(indices.presentFamily)) {
                createInfo.imageSharingMode(VK12.VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
            } else {
                createInfo.imageSharingMode(VK12.VK_SHARING_MODE_EXCLUSIVE);
            }

            createInfo.preTransform(swapChainSupport.capabilities.currentTransform());
            createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);

            createInfo.oldSwapchain(1);

            LongBuffer pSwapChain = MemoryUtil.memAllocLong(1);

            int result = KHRSwapchain.
            	vkCreateSwapchainKHR
            	(
            		VKConstants.device, 
            		createInfo, 
            		null, 
            		pSwapChain
            	);
            if(result != VK12.VK_SUCCESS) {
                throw new RuntimeException("Failed to create swap chain reason: " + VKUtils.translateVulkanResult(result));
            }

            swapChain = pSwapChain.get(0);

            KHRSwapchain.vkGetSwapchainImagesKHR(VKConstants.device, swapChain, imageCount, null);

            LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

            KHRSwapchain.vkGetSwapchainImagesKHR(VKConstants.device, swapChain, imageCount, pSwapchainImages);

            swapChainImages = new ArrayList<>(imageCount.get(0));

            for(int i = 0;i < pSwapchainImages.capacity();i++) {
                swapChainImages.add(pSwapchainImages.get(i));
            }

            swapChainImageFormat = surfaceFormat.format();
            swapChainExtent = VkExtent2D.create().set(extent);
        }
		
		
	}
	
}
