package com.github.hydos.ginger;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;

import java.io.File;
import java.nio.*;
import java.util.*;
import java.util.stream.*;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.common.info.RenderAPI;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.vulkan.*;
import com.github.hydos.ginger.engine.vulkan.io.VKWindow;
import com.github.hydos.ginger.engine.vulkan.managers.*;
import com.github.hydos.ginger.engine.vulkan.misc.*;
import com.github.hydos.ginger.engine.vulkan.misc.VKModelLoader.VKMesh;
import com.github.hydos.ginger.engine.vulkan.swapchain.VKSwapchainManager;
import com.github.hydos.ginger.engine.vulkan.utils.*;

public class VulkanExample {

	public static final int UINT32_MAX = 0xFFFFFFFF;
	public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

	public static final int MAX_FRAMES_IN_FLIGHT = 2;
		
	public static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
		.collect(toSet());



	public static class QueueFamilyIndices {

		// We use Integer to use null as the empty value
		public Integer graphicsFamily;
		public Integer presentFamily;

		public boolean isComplete() {
			return graphicsFamily != null && presentFamily != null;
		}

		public int[] unique() {
			return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();
		}
	}

	public static class SwapChainSupportDetails {

		public VkSurfaceCapabilitiesKHR capabilities;
		public VkSurfaceFormatKHR.Buffer formats;
		public IntBuffer presentModes;

	}

	public static class UniformBufferObject {

		public static final int SIZEOF = 3 * 16 * Float.BYTES;

		public Matrix4f model;
		public Matrix4f view;
		public Matrix4f proj;

		public UniformBufferObject() {
			model = new Matrix4f();
			view = new Matrix4f();
			proj = new Matrix4f();
		}
	}

	// ======= METHODS ======= //

	public void run() {
		initWindow();
		initVulkan();
		mainLoop();
		cleanup();
	}

	private void initWindow() {
		Window.create(1200, 800, "Vulkan Ginger2", 60, RenderAPI.Vulkan);
		glfwSetFramebufferSizeCallback(Window.getWindow(), this::framebufferResizeCallback);
	}

	private void framebufferResizeCallback(long window, int width, int height) {
		VKVariables.framebufferResize = true;
	}

	private void initVulkan() {
		VKRegister.createInstance();
		VKWindow.createSurface();
		GingerVK.init();
		GingerVK.getInstance().createRenderers();
		VKDeviceManager.pickPhysicalDevice();
		VKDeviceManager.createLogicalDevice();
		VKUtils.createCommandPool();
		VKTextureManager.createTextureImage();
		VKTextureManager.createTextureImageView();
		VKTextureManager.createTextureSampler();
		loadModel();
		createDescriptorSetLayout();
		VKSwapchainManager.createSwapChainObjects();
		VKUtils.createSyncObjects();
	}

	private void mainLoop() {

		while(!Window.closed()) {
			if(Window.shouldRender()) {
				Frame.drawFrame();
			}
			glfwPollEvents();
		}

		// Wait for the device to complete all operations before release resources
		vkDeviceWaitIdle(VKVariables.device);
	}

	private void cleanup() {
		VKSwapchainManager.cleanupSwapChain();

		vkDestroySampler(VKVariables.device, VKVariables.textureSampler, null);
		vkDestroyImageView(VKVariables.device, VKVariables.textureImageView, null);
		vkDestroyImage(VKVariables.device, VKVariables.textureImage, null);
		vkFreeMemory(VKVariables.device, VKVariables.textureImageMemory, null);

		vkDestroyDescriptorSetLayout(VKVariables.device, VKVariables.descriptorSetLayout, null);

		vkDestroyBuffer(VKVariables.device, VKVariables.indexBuffer, null);
		vkFreeMemory(VKVariables.device, VKVariables.indexBufferMemory, null);

		vkDestroyBuffer(VKVariables.device, VKVariables.vertexBuffer, null);
		vkFreeMemory(VKVariables.device, VKVariables.vertexBufferMemory, null);

		VKVariables.inFlightFrames.forEach(frame -> {

			vkDestroySemaphore(VKVariables.device, frame.renderFinishedSemaphore(), null);
			vkDestroySemaphore(VKVariables.device, frame.imageAvailableSemaphore(), null);
			vkDestroyFence(VKVariables.device, frame.fence(), null);
		});
		VKVariables.inFlightFrames.clear();

		vkDestroyCommandPool(VKVariables.device, VKVariables.commandPool, null);

		vkDestroyDevice(VKVariables.device, null);

		vkDestroySurfaceKHR(VKVariables.instance, VKVariables.surface, null);

		vkDestroyInstance(VKVariables.instance, null);
		
		Window.destroy();
	}

	public static void createImageViews() {

		VKVariables.swapChainImageViews = new ArrayList<>(VKVariables.swapChainImages.size());

		for(long swapChainImage : VKVariables.swapChainImages) {
			VKVariables.swapChainImageViews.add(createImageView(swapChainImage, VKVariables.swapChainImageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1));
		}
	}

	private void createDescriptorSetLayout() {

		try(MemoryStack stack = stackPush()) {

			VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(2, stack);

			VkDescriptorSetLayoutBinding uboLayoutBinding = bindings.get(0);
			uboLayoutBinding.binding(0);
			uboLayoutBinding.descriptorCount(1);
			uboLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
			uboLayoutBinding.pImmutableSamplers(null);
			uboLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

			VkDescriptorSetLayoutBinding samplerLayoutBinding = bindings.get(1);
			samplerLayoutBinding.binding(1);
			samplerLayoutBinding.descriptorCount(1);
			samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
			samplerLayoutBinding.pImmutableSamplers(null);
			samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

			VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
			layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
			layoutInfo.pBindings(bindings);

			LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

			if(vkCreateDescriptorSetLayout(VKVariables.device, layoutInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create descriptor set layout");
			}
			VKVariables.descriptorSetLayout = pDescriptorSetLayout.get(0);
		}
	}

	public static void createFramebuffers() {

		VKVariables.swapChainFramebuffers = new ArrayList<>(VKVariables.swapChainImageViews.size());

		try(MemoryStack stack = stackPush()) {

			LongBuffer attachments = stack.longs(VKVariables.colorImageView, VKVariables.depthImageView, VK_NULL_HANDLE);
			LongBuffer pFramebuffer = stack.mallocLong(1);

			// Lets allocate the create info struct once and just update the pAttachments field each iteration
			VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
			framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
			framebufferInfo.renderPass(VKVariables.renderPass);
			framebufferInfo.width(VKVariables.swapChainExtent.width());
			framebufferInfo.height(VKVariables.swapChainExtent.height());
			framebufferInfo.layers(1);

			for(long imageView : VKVariables.swapChainImageViews) {

				attachments.put(2, imageView);

				framebufferInfo.pAttachments(attachments);

				if(vkCreateFramebuffer(VKVariables.device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
					throw new RuntimeException("Failed to create framebuffer");
				}

				VKVariables.swapChainFramebuffers.add(pFramebuffer.get(0));
			}
		}
	}

	public static void createColorResources() {

		try(MemoryStack stack = stackPush()) {

			LongBuffer pColorImage = stack.mallocLong(1);
			LongBuffer pColorImageMemory = stack.mallocLong(1);

			createImage(VKVariables.swapChainExtent.width(), VKVariables.swapChainExtent.height(),
				1,
				VKVariables.msaaSamples,
				VKVariables.swapChainImageFormat,
				VK_IMAGE_TILING_OPTIMAL,
				VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
				VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
				pColorImage,
				pColorImageMemory);

			VKVariables.colorImage = pColorImage.get(0);
			VKVariables.colorImageMemory = pColorImageMemory.get(0);

			VKVariables.colorImageView = createImageView(VKVariables.colorImage, VKVariables.swapChainImageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1);

			VKUtils.transitionImageLayout(VKVariables.colorImage, VKVariables.swapChainImageFormat, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, 1);
		}
	}

	public static void createDepthResources() {

		try(MemoryStack stack = stackPush()) {

			int depthFormat = findDepthFormat();

			LongBuffer pDepthImage = stack.mallocLong(1);
			LongBuffer pDepthImageMemory = stack.mallocLong(1);

			createImage(
				VKVariables.swapChainExtent.width(), VKVariables.swapChainExtent.height(),
				1,
				VKVariables. msaaSamples,
				depthFormat,
				VK_IMAGE_TILING_OPTIMAL,
				VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
				VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
				pDepthImage,
				pDepthImageMemory);

			VKVariables.depthImage = pDepthImage.get(0);
			VKVariables.depthImageMemory = pDepthImageMemory.get(0);

			VKVariables.depthImageView = createImageView(VKVariables.depthImage, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, 1);

			// Explicitly transitioning the depth image
			VKUtils.transitionImageLayout(VKVariables.depthImage, depthFormat,
				VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
				1);

		}
	}

	private static int findSupportedFormat(IntBuffer formatCandidates, int tiling, int features) {

		try(MemoryStack stack = stackPush()) {

			VkFormatProperties props = VkFormatProperties.callocStack(stack);

			for(int i = 0; i < formatCandidates.capacity(); ++i) {

				int format = formatCandidates.get(i);

				vkGetPhysicalDeviceFormatProperties(VKVariables.physicalDevice, format, props);

				if(tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features) {
					return format;
				} else if(tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features) {
					return format;
				}

			}
		}

		throw new RuntimeException("Failed to find supported format");
	}


	public static int findDepthFormat() {
		return findSupportedFormat(
			stackGet().ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT),
			VK_IMAGE_TILING_OPTIMAL,
			VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT);
	}
	
	private void loadModel() {

		File modelFile = new File(ClassLoader.getSystemClassLoader().getResource("models/chalet.obj").getFile());

		VKMesh model = VKModelLoader.loadModel(modelFile, aiProcess_FlipUVs | aiProcess_DropNormals);
		GingerVK.getInstance().entityRenderer.processEntity(model);
	}

	public static double log2(double n) {
		return Math.log(n) / Math.log(2);
	}
	public static void generateMipmaps(long image, int imageFormat, int width, int height, int mipLevels) {

		try(MemoryStack stack = stackPush()) {

			// Check if image format supports linear blitting
			VkFormatProperties formatProperties = VkFormatProperties.mallocStack(stack);
			vkGetPhysicalDeviceFormatProperties(VKVariables.physicalDevice, imageFormat, formatProperties);

			if((formatProperties.optimalTilingFeatures() & VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT) == 0) {
				throw new RuntimeException("Texture image format does not support linear blitting");
			}

			VkCommandBuffer commandBuffer = CommandBufferManager.beginSingleTimeCommands();

			VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack);
			barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
			barrier.image(image);
			barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			barrier.dstAccessMask(VK_QUEUE_FAMILY_IGNORED);
			barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
			barrier.subresourceRange().baseArrayLayer(0);
			barrier.subresourceRange().layerCount(1);
			barrier.subresourceRange().levelCount(1);

			int mipWidth = width;
			int mipHeight = height;

			for(int i = 1;i < mipLevels;i++) {

				barrier.subresourceRange().baseMipLevel(i - 1);
				barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				barrier.newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
				barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
				barrier.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);

				vkCmdPipelineBarrier(commandBuffer,
					VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, 0,
					null,
					null,
					barrier);

				VkImageBlit.Buffer blit = VkImageBlit.callocStack(1, stack);
				blit.srcOffsets(0).set(0, 0, 0);
				blit.srcOffsets(1).set(mipWidth, mipHeight, 1);
				blit.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
				blit.srcSubresource().mipLevel(i - 1);
				blit.srcSubresource().baseArrayLayer(0);
				blit.srcSubresource().layerCount(1);
				blit.dstOffsets(0).set(0, 0, 0);
				blit.dstOffsets(1).set(mipWidth > 1 ? mipWidth / 2 : 1, mipHeight > 1 ? mipHeight / 2 : 1, 1);
				blit.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
				blit.dstSubresource().mipLevel(i);
				blit.dstSubresource().baseArrayLayer(0);
				blit.dstSubresource().layerCount(1);

				vkCmdBlitImage(commandBuffer,
					image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
					image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
					blit,
					VK_FILTER_LINEAR);

				barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
				barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
				barrier.srcAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
				barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

				vkCmdPipelineBarrier(commandBuffer,
					VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0,
					null,
					null,
					barrier);

				if(mipWidth > 1) {
					mipWidth /= 2;
				}

				if(mipHeight > 1) {
					mipHeight /= 2;
				}
			}

			barrier.subresourceRange().baseMipLevel(mipLevels - 1);
			barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
			barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

			vkCmdPipelineBarrier(commandBuffer,
				VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0,
				null,
				null,
				barrier);

			CommandBufferManager.endSingleTimeCommands(commandBuffer);
		}
	}

	public static int getMaxUsableSampleCount() {

		try(MemoryStack stack = stackPush()) {

			VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.mallocStack(stack);
			vkGetPhysicalDeviceProperties(VKVariables.physicalDevice, physicalDeviceProperties);

			int sampleCountFlags = physicalDeviceProperties.limits().framebufferColorSampleCounts()
				& physicalDeviceProperties.limits().framebufferDepthSampleCounts();

			if((sampleCountFlags & VK_SAMPLE_COUNT_64_BIT) != 0) {
				return VK_SAMPLE_COUNT_64_BIT;
			}
			if((sampleCountFlags & VK_SAMPLE_COUNT_32_BIT) != 0) {
				return VK_SAMPLE_COUNT_32_BIT;
			}
			if((sampleCountFlags & VK_SAMPLE_COUNT_16_BIT) != 0) {
				return VK_SAMPLE_COUNT_16_BIT;
			}
			if((sampleCountFlags & VK_SAMPLE_COUNT_8_BIT) != 0) {
				return VK_SAMPLE_COUNT_8_BIT;
			}
			if((sampleCountFlags & VK_SAMPLE_COUNT_4_BIT) != 0) {
				return VK_SAMPLE_COUNT_4_BIT;
			}
			if((sampleCountFlags & VK_SAMPLE_COUNT_2_BIT) != 0) {
				return VK_SAMPLE_COUNT_2_BIT;
			}

			return VK_SAMPLE_COUNT_1_BIT;
		}
	}

	public static long createImageView(long image, int format, int aspectFlags, int mipLevels) {

		try(MemoryStack stack = stackPush()) {

			VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
			viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
			viewInfo.image(image);
			viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
			viewInfo.format(format);
			viewInfo.subresourceRange().aspectMask(aspectFlags);
			viewInfo.subresourceRange().baseMipLevel(0);
			viewInfo.subresourceRange().levelCount(mipLevels);
			viewInfo.subresourceRange().baseArrayLayer(0);
			viewInfo.subresourceRange().layerCount(1);

			LongBuffer pImageView = stack.mallocLong(1);

			if(vkCreateImageView(VKVariables.device, viewInfo, null, pImageView) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create texture image view");
			}

			return pImageView.get(0);
		}
	}

	public static void createImage(int width, int height, int mipLevels, int numSamples, int format, int tiling, int usage, int memProperties,
		LongBuffer pTextureImage, LongBuffer pTextureImageMemory) {

		try(MemoryStack stack = stackPush()) {

			VkImageCreateInfo imageInfo = VkImageCreateInfo.callocStack(stack);
			imageInfo.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
			imageInfo.imageType(VK_IMAGE_TYPE_2D);
			imageInfo.extent().width(width);
			imageInfo.extent().height(height);
			imageInfo.extent().depth(1);
			imageInfo.mipLevels(mipLevels);
			imageInfo.arrayLayers(1);
			imageInfo.format(format);
			imageInfo.tiling(tiling);
			imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
			imageInfo.usage(usage);
			imageInfo.samples(numSamples);
			imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

			if(vkCreateImage(VKVariables.device, imageInfo, null, pTextureImage) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create image");
			}

			VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
			vkGetImageMemoryRequirements(VKVariables.device, pTextureImage.get(0), memRequirements);

			VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
			allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
			allocInfo.allocationSize(memRequirements.size());
			allocInfo.memoryTypeIndex(VKUtils.findMemoryType(memRequirements.memoryTypeBits(), memProperties));

			if(vkAllocateMemory(VKVariables.device, allocInfo, null, pTextureImageMemory) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate image memory");
			}

			vkBindImageMemory(VKVariables.device, pTextureImage.get(0), pTextureImageMemory.get(0), 0);
		}
	}


	public static void main(String[] args) {

		VulkanExample app = new VulkanExample();

		app.run();
	}

}
