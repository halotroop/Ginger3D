package com.github.hydos.ginger.engine.vulkan.swapchain;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.*;
import java.util.ArrayList;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.VulkanExample.VulkanDemoGinger2;
import com.github.hydos.ginger.VulkanExample.VulkanDemoGinger2.*;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.vulkan.pipelines.VKPipelineManager;

public class VKSwapchainManager
{
	
    public static void cleanupSwapChain() {

        vkDestroyImageView(VulkanDemoGinger2.device, VulkanDemoGinger2.colorImageView, null);
        vkDestroyImage(VulkanDemoGinger2.device, VulkanDemoGinger2.colorImage, null);
        vkFreeMemory(VulkanDemoGinger2.device, VulkanDemoGinger2.colorImageMemory, null);

        vkDestroyImageView(VulkanDemoGinger2.device, VulkanDemoGinger2.depthImageView, null);
        vkDestroyImage(VulkanDemoGinger2.device, VulkanDemoGinger2.depthImage, null);
        vkFreeMemory(VulkanDemoGinger2.device, VulkanDemoGinger2.depthImageMemory, null);

        VulkanDemoGinger2.uniformBuffers.forEach(ubo -> vkDestroyBuffer(VulkanDemoGinger2.device, ubo, null));
        VulkanDemoGinger2.uniformBuffersMemory.forEach(uboMemory -> vkFreeMemory(VulkanDemoGinger2.device, uboMemory, null));

        vkDestroyDescriptorPool(VulkanDemoGinger2.device, VulkanDemoGinger2.descriptorPool, null);

        VulkanDemoGinger2.swapChainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(VulkanDemoGinger2.device, framebuffer, null));

        vkFreeCommandBuffers(VulkanDemoGinger2.device, VulkanDemoGinger2.commandPool, VulkanDemoGinger2.asPointerBuffer(VulkanDemoGinger2.commandBuffers));

        vkDestroyPipeline(VulkanDemoGinger2.device, VulkanDemoGinger2.graphicsPipeline, null);

        vkDestroyPipelineLayout(VulkanDemoGinger2.device, VulkanDemoGinger2.pipelineLayout, null);

        vkDestroyRenderPass(VulkanDemoGinger2.device, VulkanDemoGinger2.renderPass, null);

        VulkanDemoGinger2.swapChainImageViews.forEach(imageView -> vkDestroyImageView(VulkanDemoGinger2.device, imageView, null));

        vkDestroySwapchainKHR(VulkanDemoGinger2.device, VulkanDemoGinger2.swapChain, null);
    }
    
    public static void recreateSwapChain() {

        try(MemoryStack stack = stackPush()) {

            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);

            while(width.get(0) == 0 && height.get(0) == 0) {
                glfwGetFramebufferSize(Window.getWindow(), width, height);
                glfwWaitEvents();
            }
        }

        vkDeviceWaitIdle(VulkanDemoGinger2.device);

        VKSwapchainManager.cleanupSwapChain();

        createSwapChainObjects();
    }
    
    public static void createSwapChain() {

        try(MemoryStack stack = stackPush()) {

            SwapChainSupportDetails swapChainSupport = VulkanDemoGinger2.querySwapChainSupport(VulkanDemoGinger2.physicalDevice, stack);

            VkSurfaceFormatKHR surfaceFormat = VulkanDemoGinger2.chooseSwapSurfaceFormat(swapChainSupport.formats);
            int presentMode = VulkanDemoGinger2.chooseSwapPresentMode(swapChainSupport.presentModes);
            VkExtent2D extent = VulkanDemoGinger2.chooseSwapExtent(swapChainSupport.capabilities);

            IntBuffer imageCount = stack.ints(swapChainSupport.capabilities.minImageCount() + 1);

            if(swapChainSupport.capabilities.maxImageCount() > 0 && imageCount.get(0) > swapChainSupport.capabilities.maxImageCount()) {
                imageCount.put(0, swapChainSupport.capabilities.maxImageCount());
            }

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            createInfo.surface(VulkanDemoGinger2.surface);

            // Image settings
            createInfo.minImageCount(imageCount.get(0));
            createInfo.imageFormat(surfaceFormat.format());
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(extent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            QueueFamilyIndices indices = VulkanDemoGinger2.findQueueFamilies(VulkanDemoGinger2.physicalDevice);

            if(!indices.graphicsFamily.equals(indices.presentFamily)) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            createInfo.preTransform(swapChainSupport.capabilities.currentTransform());
            createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);

            createInfo.oldSwapchain(VK_NULL_HANDLE);

            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);

            if(vkCreateSwapchainKHR(VulkanDemoGinger2.device, createInfo, null, pSwapChain) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create swap chain");
            }

            VulkanDemoGinger2.swapChain = pSwapChain.get(0);

            vkGetSwapchainImagesKHR(VulkanDemoGinger2.device, VulkanDemoGinger2.swapChain, imageCount, null);

            LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

            vkGetSwapchainImagesKHR(VulkanDemoGinger2.device, VulkanDemoGinger2.swapChain, imageCount, pSwapchainImages);

            VulkanDemoGinger2.swapChainImages = new ArrayList<>(imageCount.get(0));

            for(int i = 0;i < pSwapchainImages.capacity();i++) {
            	VulkanDemoGinger2.swapChainImages.add(pSwapchainImages.get(i));
            }

            VulkanDemoGinger2.swapChainImageFormat = surfaceFormat.format();
            VulkanDemoGinger2.swapChainExtent = VkExtent2D.create().set(extent);
        }
    }
    
    
    /**
     * i tried organising it but if i change the order everything breaks
     */
    public static void createSwapChainObjects() {
    	createSwapChain();
    	VulkanDemoGinger2.createImageViews();
    	VulkanDemoGinger2.createRenderPass();
        VKPipelineManager.createGraphicsPipeline();
        VulkanDemoGinger2.createColorResources();
        VulkanDemoGinger2.createDepthResources();
        VulkanDemoGinger2.createFramebuffers();
        VulkanDemoGinger2.createUniformBuffers();
        VulkanDemoGinger2.createDescriptorPool();
        VulkanDemoGinger2.createDescriptorSets();
        VulkanDemoGinger2.createCommandBuffers();
    }
	
}
