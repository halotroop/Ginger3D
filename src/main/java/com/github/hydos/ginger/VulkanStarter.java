package com.github.hydos.ginger;

import java.io.IOException;
import java.nio.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.common.info.RenderAPI;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.common.obj.ModelLoader;
import com.github.hydos.ginger.engine.vulkan.*;
import com.github.hydos.ginger.engine.vulkan.api.VKGinger;
import com.github.hydos.ginger.engine.vulkan.model.*;
import com.github.hydos.ginger.engine.vulkan.render.renderers.*;
import com.github.hydos.ginger.engine.vulkan.render.ubo.*;
import com.github.hydos.ginger.engine.vulkan.shaders.*;
import com.github.hydos.ginger.engine.vulkan.utils.*;

/** @author hydos06
 *         the non ARR vulkan test example */
public class VulkanStarter
{
	public static boolean getSupportedDepthFormat(VkPhysicalDevice physicalDevice, IntBuffer depthFormat)
	{
		// Since all depth formats may be optional, we need to find a suitable depth format to use
		// Start with the highest precision packed format
		int[] depthFormats =
		{
			VK12.VK_FORMAT_D32_SFLOAT_S8_UINT,
			VK12.VK_FORMAT_D32_SFLOAT,
			VK12.VK_FORMAT_D24_UNORM_S8_UINT,
			VK12.VK_FORMAT_D16_UNORM_S8_UINT,
			VK12.VK_FORMAT_D16_UNORM
		};
		VkFormatProperties formatProps = VkFormatProperties.calloc();
		for (int format : depthFormats)
		{
			VK12.vkGetPhysicalDeviceFormatProperties(physicalDevice, format, formatProps);
			// Format must support depth stencil attachment for optimal tiling
			if ((formatProps.optimalTilingFeatures() & VK12.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0)
			{
				depthFormat.put(0, format);
				return true;
			}
		}
		return false;
	}

	public static class ColorAndDepthFormatAndSpace
	{
		public int colorFormat;
		public int colorSpace;
		public int depthFormat;
	}

	private static long createCommandPool(VkDevice device, int queueNodeIndex)
	{
		VkCommandPoolCreateInfo cmdPoolInfo = VkCommandPoolCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
			.queueFamilyIndex(queueNodeIndex)
			.flags(VK12.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
		LongBuffer pCmdPool = MemoryUtil.memAllocLong(1);
		int err = VK12.vkCreateCommandPool(device, cmdPoolInfo, null, pCmdPool);
		long commandPool = pCmdPool.get(0);
		cmdPoolInfo.free();
		MemoryUtil.memFree(pCmdPool);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create command pool: " + VKUtils.translateVulkanResult(err)); }
		return commandPool;
	}

	private static VkQueue createDeviceQueue(VkDevice device, int queueFamilyIndex)
	{
		PointerBuffer pQueue = MemoryUtil.memAllocPointer(1);
		VK12.vkGetDeviceQueue(device, queueFamilyIndex, 0, pQueue);
		long queue = pQueue.get(0);
		MemoryUtil.memFree(pQueue);
		return new VkQueue(queue, device);
	}

	private static VkCommandBuffer createCommandBuffer(VkDevice device, long commandPool)
	{
		VkCommandBufferAllocateInfo cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
			.commandPool(commandPool)
			.level(VK12.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
			.commandBufferCount(1);
		PointerBuffer pCommandBuffer = MemoryUtil.memAllocPointer(1);
		int err = VK12.vkAllocateCommandBuffers(device, cmdBufAllocateInfo, pCommandBuffer);
		cmdBufAllocateInfo.free();
		long commandBuffer = pCommandBuffer.get(0);
		MemoryUtil.memFree(pCommandBuffer);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to allocate command buffer: " + VKUtils.translateVulkanResult(err)); }
		return new VkCommandBuffer(commandBuffer, device);
	}

	public static class Swapchain
	{
		public long swapchainHandle;
		public long[] images;
		public long[] imageViews;
	}

	public static class DepthStencil
	{
		public long view;
	}

	private static void submitCommandBuffer(VkQueue queue, VkCommandBuffer commandBuffer)
	{
		if (commandBuffer == null || commandBuffer.address() == MemoryUtil.NULL)
			return;
		VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_SUBMIT_INFO);
		PointerBuffer pCommandBuffers = MemoryUtil.memAllocPointer(1)
			.put(commandBuffer)
			.flip();
		submitInfo.pCommandBuffers(pCommandBuffers);
		int err = VK12.vkQueueSubmit(queue, submitInfo, VK12.VK_NULL_HANDLE);
		MemoryUtil.memFree(pCommandBuffers);
		submitInfo.free();
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to submit command buffer: " + VKUtils.translateVulkanResult(err)); }
	}

	private static long createDescriptorPool(VkDevice device)
	{
		// We need to tell the API the number of max. requested descriptors per type
		VkDescriptorPoolSize.Buffer typeCounts = VkDescriptorPoolSize.calloc(1)
			// This example only uses one descriptor type (uniform buffer) and only
			// requests one descriptor of this type
			.type(VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1);
		// For additional types you need to add new entries in the type count list
		// E.g. for two combined image samplers :
		// typeCounts[1].type = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
		// typeCounts[1].descriptorCount = 2;
		// Create the global descriptor pool
		// All descriptors used in this example are allocated from this pool
		VkDescriptorPoolCreateInfo descriptorPoolInfo = VkDescriptorPoolCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
			.pPoolSizes(typeCounts)
			// Set the max. number of sets that can be requested
			// Requesting descriptors beyond maxSets will result in an error
			.maxSets(1);
		LongBuffer pDescriptorPool = MemoryUtil.memAllocLong(1);
		int err = VK12.vkCreateDescriptorPool(device, descriptorPoolInfo, null, pDescriptorPool);
		long descriptorPool = pDescriptorPool.get(0);
		MemoryUtil.memFree(pDescriptorPool);
		descriptorPoolInfo.free();
		typeCounts.free();
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create descriptor pool: " + VKUtils.translateVulkanResult(err)); }
		return descriptorPool;
	}

	private static long createDescriptorSet(VkDevice device, long descriptorPool, long descriptorSetLayout, UboDescriptor uniformDataVSDescriptor)
	{
		LongBuffer pDescriptorSetLayout = MemoryUtil.memAllocLong(1);
		pDescriptorSetLayout.put(0, descriptorSetLayout);
		VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
			.descriptorPool(descriptorPool)
			.pSetLayouts(pDescriptorSetLayout);
		LongBuffer pDescriptorSet = MemoryUtil.memAllocLong(1);
		int err = VK12.vkAllocateDescriptorSets(device, allocInfo, pDescriptorSet);
		long descriptorSet = pDescriptorSet.get(0);
		MemoryUtil.memFree(pDescriptorSet);
		allocInfo.free();
		MemoryUtil.memFree(pDescriptorSetLayout);
		if (err != VK12.VK_SUCCESS)
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
			.sType(VK12.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
			.dstSet(descriptorSet)
			.descriptorCount(1)
			.descriptorType(VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.pBufferInfo(descriptor)
			.dstBinding(0); // Binds this uniform buffer to binding point 0
		VK12.vkUpdateDescriptorSets(device, writeDescriptorSet, null);
		writeDescriptorSet.free();
		descriptor.free();
		return descriptorSet;
	}

	private static long createDescriptorSetLayout(VkDevice device)
	{
		int err;
		// One binding for a UBO used in a vertex shader
		VkDescriptorSetLayoutBinding.Buffer layoutBinding = VkDescriptorSetLayoutBinding.calloc(1)
			.binding(ShaderType.vertexShader) // <- Binding 0 : Uniform buffer (Vertex shader)
			.descriptorType(VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1)
			.stageFlags(VK12.VK_SHADER_STAGE_VERTEX_BIT);
		// Build a create-info struct to create the descriptor set layout
		VkDescriptorSetLayoutCreateInfo descriptorLayout = VkDescriptorSetLayoutCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
			.pBindings(layoutBinding);
		LongBuffer pDescriptorSetLayout = MemoryUtil.memAllocLong(1);
		err = VK12.vkCreateDescriptorSetLayout(device, descriptorLayout, null, pDescriptorSetLayout);
		long descriptorSetLayout = pDescriptorSetLayout.get(0);
		MemoryUtil.memFree(pDescriptorSetLayout);
		descriptorLayout.free();
		layoutBinding.free();
		if (err != VK12.VK_SUCCESS)
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
		Window.create(1200, 600, "Litecraft Vulkan", 60, RenderAPI.Vulkan);
		new VKGinger();
		/* Look for instance extensions */
		PointerBuffer requiredExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
		if (requiredExtensions == null)
		{ throw new AssertionError("Failed to find list of required Vulkan extensions"); }
		// Create the Vulkan instance
		final VkInstance vulkanInstance = VKLoader.createInstance(requiredExtensions);
		VKUtils.setupVulkanDebugCallback();
		final long debugCallbackHandle = VKUtils.startVulkanDebugging(vulkanInstance, EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT | EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT, VKConstants.debugCallback);
		final VkPhysicalDevice physicalDevice = VKDeviceProperties.getFirstPhysicalDevice(vulkanInstance);
		final VKDeviceProperties deviceAndGraphicsQueueFamily = VKDeviceProperties.initDeviceProperties(physicalDevice);
		final VkDevice device = deviceAndGraphicsQueueFamily.device;
		int queueFamilyIndex = deviceAndGraphicsQueueFamily.queueFamilyIndex;
		final VkPhysicalDeviceMemoryProperties memoryProperties = deviceAndGraphicsQueueFamily.memoryProperties;
		GLFWKeyCallback keyCallback;
		GLFW.glfwSetKeyCallback(Window.getWindow(), keyCallback = new GLFWKeyCallback()
		{
			public void invoke(long window, int key, int scancode, int action, int mods)
			{
				if (action != GLFW.GLFW_RELEASE)
					return;
				if (key == GLFW.GLFW_KEY_ESCAPE)
					GLFW.glfwSetWindowShouldClose(window, true);
			}
		});
		LongBuffer pSurface = MemoryUtil.memAllocLong(1);
		int err = GLFWVulkan.glfwCreateWindowSurface(vulkanInstance, Window.getWindow(), null, pSurface);
		final long surface = pSurface.get(0);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create surface: " + VKUtils.translateVulkanResult(err)); }
		// Create static Vulkan resources
		final ColorAndDepthFormatAndSpace colorAndDepthFormatAndSpace = VKMasterRenderer.getColorFormatAndSpace(physicalDevice, surface);
		final long commandPool = createCommandPool(device, queueFamilyIndex);
		final VkCommandBuffer setupCommandBuffer = createCommandBuffer(device, commandPool);
		final VkQueue queue = createDeviceQueue(device, queueFamilyIndex);
		final long renderPass = ExampleRenderer.createRenderPass(device, colorAndDepthFormatAndSpace.colorFormat, colorAndDepthFormatAndSpace.depthFormat);
		final long renderCommandPool = createCommandPool(device, queueFamilyIndex);
		VKVertices vertices = VKModelConverter.convertModel(ModelLoader.getCubeMesh(), memoryProperties, device);
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
					.sType(VK12.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
				int err = VK12.vkBeginCommandBuffer(setupCommandBuffer, cmdBufInfo);
				cmdBufInfo.free();
				if (err != VK12.VK_SUCCESS)
				{ throw new AssertionError("Failed to begin setup command buffer: " + VKUtils.translateVulkanResult(err)); }
				long oldChain = swapchain != null ? swapchain.swapchainHandle : VK12.VK_NULL_HANDLE;
				// Create the swapchain (this will also add a memory barrier to initialize the framebuffer images)
				swapchain = VKMasterRenderer.createSwapChain(device, physicalDevice, surface, oldChain, setupCommandBuffer,
					Window.getWidth(), Window.getHeight(), colorAndDepthFormatAndSpace.colorFormat, colorAndDepthFormatAndSpace.colorSpace);
				// Create depth-stencil image
				depthStencil = VKMasterRenderer.createDepthStencil(device, memoryProperties, colorAndDepthFormatAndSpace.depthFormat, setupCommandBuffer);
				err = VK12.vkEndCommandBuffer(setupCommandBuffer);
				if (err != VK12.VK_SUCCESS)
				{ throw new AssertionError("Failed to end setup command buffer: " + VKUtils.translateVulkanResult(err)); }
				submitCommandBuffer(queue, setupCommandBuffer);
				VK12.vkQueueWaitIdle(queue);
				if (framebuffers != null)
				{ for (int i = 0; i < framebuffers.length; i++)
					VK12.vkDestroyFramebuffer(device, framebuffers[i], null); }
				framebuffers = ExampleRenderer.createFramebuffers(device, swapchain, renderPass, Window.getWidth(), Window.getHeight(), depthStencil);
				// Create render command buffers
				if (renderCommandBuffers != null)
				{ VK12.vkResetCommandPool(device, renderCommandPool, VKUtils.VK_FLAGS_NONE); }
				renderCommandBuffers = VKUtils.initRenderCommandBuffers(device, renderCommandPool, framebuffers, renderPass, Window.getWidth(), Window.getHeight(), pipeline, descriptorSet,
					vertices.vkVerticiesBuffer);
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
		GLFW.glfwSetFramebufferSizeCallback(Window.getWindow(), framebufferSizeCallback);
		GLFW.glfwShowWindow(Window.getWindow());
		// Pre-allocate everything needed in the render loop
		IntBuffer pImageIndex = MemoryUtil.memAllocInt(1);
		int currentBuffer = 0;
		PointerBuffer pCommandBuffers = MemoryUtil.memAllocPointer(1);
		LongBuffer pSwapchains = MemoryUtil.memAllocLong(1);
		LongBuffer pImageAcquiredSemaphore = MemoryUtil.memAllocLong(1);
		LongBuffer pRenderCompleteSemaphore = MemoryUtil.memAllocLong(1);
		// Info struct to create a semaphore
		VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
		// Info struct to submit a command buffer which will wait on the semaphore
		IntBuffer pWaitDstStageMask = MemoryUtil.memAllocInt(1);
		pWaitDstStageMask.put(0, VK12.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_SUBMIT_INFO)
			.waitSemaphoreCount(pImageAcquiredSemaphore.remaining())
			.pWaitSemaphores(pImageAcquiredSemaphore)
			.pWaitDstStageMask(pWaitDstStageMask)
			.pCommandBuffers(pCommandBuffers)
			.pSignalSemaphores(pRenderCompleteSemaphore);
		// Info struct to present the current swapchain image to the display
		VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc()
			.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
			.pWaitSemaphores(pRenderCompleteSemaphore)
			.swapchainCount(pSwapchains.remaining())
			.pSwapchains(pSwapchains)
			.pImageIndices(pImageIndex)
			.pResults(null);
		// The render loop
		long lastTime = System.nanoTime();
		float time = 0.0f;
		while (!GLFW.glfwWindowShouldClose(Window.getWindow()))
		{
			GLFW.glfwPollEvents();
			if (swapchainRecreator.mustRecreate)
				swapchainRecreator.recreate();
			
			
			err = VK12.vkCreateSemaphore(device, semaphoreCreateInfo, null, pImageAcquiredSemaphore);
			if (err != VK12.VK_SUCCESS)
			{ throw new AssertionError("Failed to create image acquired semaphore: " + VKUtils.translateVulkanResult(err)); }
			// Create a semaphore to wait for the render to complete, before presenting
			err = VK12.vkCreateSemaphore(device, semaphoreCreateInfo, null, pRenderCompleteSemaphore);
			if (err != VK12.VK_SUCCESS)
			{ throw new AssertionError("Failed to create render complete semaphore: " + VKUtils.translateVulkanResult(err)); }
			// Get next image from the swap chain (back/front buffer).
			// This will setup the imageAquiredSemaphore to be signalled when the operation is complete
			err = KHRSwapchain.vkAcquireNextImageKHR(device, swapchain.swapchainHandle, VKConstants.MAX_UNSIGNED_INT, pImageAcquiredSemaphore.get(0), VK12.VK_NULL_HANDLE, pImageIndex);
			currentBuffer = pImageIndex.get(0);
			if (err != VK12.VK_SUCCESS)
			{ throw new AssertionError("Failed to acquire next swapchain image: " + VKUtils.translateVulkanResult(err)); }
			// Select the command buffer for the current framebuffer image/attachment
			pCommandBuffers.put(0, renderCommandBuffers[currentBuffer]);
			// Update UBO
			long thisTime = System.nanoTime();
			time += (thisTime - lastTime) / 1E9f;
			lastTime = thisTime;
			ubo.updateUbo(device, time);
			// Submit to the graphics queue
			
			err = VK12.vkQueueSubmit(queue, submitInfo, VK12.VK_NULL_HANDLE);
			if (err != VK12.VK_SUCCESS)
			{ throw new AssertionError("Failed to submit render queue: " + VKUtils.translateVulkanResult(err)); }
			// Present the current buffer to the swap chain
			// This will display the image
			pSwapchains.put(0, swapchain.swapchainHandle);
			err = KHRSwapchain.vkQueuePresentKHR(queue, presentInfo);
			if (err != VK12.VK_SUCCESS)
			{ throw new AssertionError("Failed to present the swapchain image: " + VKUtils.translateVulkanResult(err)); }
			// Create and submit post present barrier
			VK12.vkQueueWaitIdle(queue);
			// Destroy this semaphore (we will create a new one in the next frame)
			VK12.vkDestroySemaphore(device, pImageAcquiredSemaphore.get(0), null);
			VK12.vkDestroySemaphore(device, pRenderCompleteSemaphore.get(0), null);
		}
		VKGinger.getInstance().end(pWaitDstStageMask, pImageAcquiredSemaphore, pRenderCompleteSemaphore, pSwapchains, pCommandBuffers, semaphoreCreateInfo, submitInfo, presentInfo, vulkanInstance, debugCallbackHandle, framebufferSizeCallback, keyCallback);
	}
}