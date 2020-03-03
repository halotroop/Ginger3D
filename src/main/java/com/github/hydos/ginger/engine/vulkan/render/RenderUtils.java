package com.github.hydos.ginger.engine.vulkan.render;

import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import com.github.hydos.ginger.VulkanStarter.DepthStencil;
import com.github.hydos.ginger.VulkanStarter.Swapchain;
import com.github.hydos.ginger.engine.vulkan.memory.VKMemory;
import com.github.hydos.ginger.engine.vulkan.model.VKVertices;
import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;

public class RenderUtils
{
	
	public static long createRenderPass(VkDevice device, int colorFormat, int depthFormat)
	{
		VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2);
		attachments.get(0) // <- color attachment
			.format(colorFormat)
			.samples(VK12.VK_SAMPLE_COUNT_1_BIT)
			.loadOp(VK12.VK_ATTACHMENT_LOAD_OP_CLEAR)
			.storeOp(VK12.VK_ATTACHMENT_STORE_OP_STORE)
			.stencilLoadOp(VK12.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
			.stencilStoreOp(VK12.VK_ATTACHMENT_STORE_OP_DONT_CARE)
			.initialLayout(VK12.VK_IMAGE_LAYOUT_UNDEFINED)
			.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
		attachments.get(1) // <- depth-stencil attachment
			.format(depthFormat)
			.samples(VK12.VK_SAMPLE_COUNT_1_BIT)
			.loadOp(VK12.VK_ATTACHMENT_LOAD_OP_CLEAR)
			.storeOp(VK12.VK_ATTACHMENT_STORE_OP_STORE)
			.stencilLoadOp(VK12.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
			.stencilStoreOp(VK12.VK_ATTACHMENT_STORE_OP_DONT_CARE)
			.initialLayout(VK12.VK_IMAGE_LAYOUT_UNDEFINED)
			.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
		VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1)
			.attachment(0)
			.layout(VK12.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		VkAttachmentReference depthReference = VkAttachmentReference.calloc()
			.attachment(1)
			.layout(VK12.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1)
			.pipelineBindPoint(VK12.VK_PIPELINE_BIND_POINT_GRAPHICS)
			.colorAttachmentCount(colorReference.remaining())
			.pColorAttachments(colorReference) // <- only color attachment
			.pDepthStencilAttachment(depthReference) // <- and depth-stencil
		;
		VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
			.pAttachments(attachments)
			.pSubpasses(subpass);
		LongBuffer pRenderPass = memAllocLong(1);
		int err = VK12.vkCreateRenderPass(device, renderPassInfo, null, pRenderPass);
		long renderPass = pRenderPass.get(0);
		memFree(pRenderPass);
		renderPassInfo.free();
		depthReference.free();
		colorReference.free();
		subpass.free();
		attachments.free();
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create clear render pass: " + VKUtils.translateVulkanResult(err)); }
		return renderPass;
	}

	public static long[] createFramebuffers(VkDevice device, Swapchain swapchain, long renderPass, int width, int height, DepthStencil depthStencil)
	{
		LongBuffer attachments = memAllocLong(2);
		attachments.put(1, depthStencil.view);
		VkFramebufferCreateInfo fci = VkFramebufferCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
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
			int err = VK12.vkCreateFramebuffer(device, fci, null, pFramebuffer);
			long framebuffer = pFramebuffer.get(0);
			if (err != VK12.VK_SUCCESS)
			{ throw new AssertionError("Failed to create framebuffer: " + VKUtils.translateVulkanResult(err)); }
			framebuffers[i] = framebuffer;
		}
		memFree(attachments);
		memFree(pFramebuffer);
		fci.free();
		return framebuffers;
	}
	
	public static VKVertices createVertices(VkPhysicalDeviceMemoryProperties deviceMemoryProperties, VkDevice device)
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
			.sType(VK12.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
		VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		int err;
		// Generate vertex buffer
		//  Setup
		VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
			.sType(VK12.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
			.size(vertexBuffer.remaining())
			.usage(VK12.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		LongBuffer pBuffer = memAllocLong(1);
		err = VK12.vkCreateBuffer(device, bufInfo, null, pBuffer);
		long verticesBuf = pBuffer.get(0);
		memFree(pBuffer);
		bufInfo.free();
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create vertex buffer: " + VKUtils.translateVulkanResult(err)); }
		VK12.vkGetBufferMemoryRequirements(device, verticesBuf, memReqs);
		memAlloc.allocationSize(memReqs.size());
		IntBuffer memoryTypeIndex = memAllocInt(1);
		VKMemory.getMemoryType(deviceMemoryProperties, memReqs.memoryTypeBits(), VK12.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, memoryTypeIndex);
		memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		memFree(memoryTypeIndex);
		memReqs.free();
		LongBuffer pMemory = memAllocLong(1);
		err = VK12.vkAllocateMemory(device, memAlloc, null, pMemory);
		long verticesMem = pMemory.get(0);
		memFree(pMemory);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to allocate vertex memory: " + VKUtils.translateVulkanResult(err)); }
		PointerBuffer pData = memAllocPointer(1);
		err = VK12.vkMapMemory(device, verticesMem, 0, vertexBuffer.remaining(), 0, pData);
		memAlloc.free();
		long data = pData.get(0);
		memFree(pData);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to map vertex memory: " + VKUtils.translateVulkanResult(err)); }
		memCopy(memAddress(vertexBuffer), data, vertexBuffer.remaining());
		memFree(vertexBuffer);
		VK12.vkUnmapMemory(device, verticesMem);
		err = VK12.vkBindBufferMemory(device, verticesBuf, verticesMem, 0);
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to bind memory to vertex buffer: " + VKUtils.translateVulkanResult(err)); }
		// Binding description
		VkVertexInputBindingDescription.Buffer bindingDescriptor = VkVertexInputBindingDescription.calloc(1)
			.binding(0) // <- we bind our vertex buffer to point 0
			.stride((3 + 3) * 4)
			.inputRate(VK12.VK_VERTEX_INPUT_RATE_VERTEX);
		// Attribute descriptions
		// Describes memory layout and shader attribute locations
		VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2);
		// Location 0 : Position
		attributeDescriptions.get(0)
			.binding(0) // <- binding point used in the VkVertexInputBindingDescription
			.location(0) // <- location in the shader's attribute layout (inside the shader source)
			.format(VK12.VK_FORMAT_R32G32B32_SFLOAT)
			.offset(0);
		// Location 1 : Color
		attributeDescriptions.get(1)
			.binding(0) // <- binding point used in the VkVertexInputBindingDescription
			.location(1) // <- location in the shader's attribute layout (inside the shader source)
			.format(VK12.VK_FORMAT_R32G32B32_SFLOAT)
			.offset(3 * 4);
		// Assign to vertex buffer
		VkPipelineVertexInputStateCreateInfo vi = VkPipelineVertexInputStateCreateInfo.calloc();
		vi.sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
		vi.pVertexBindingDescriptions(bindingDescriptor);
		vi.pVertexAttributeDescriptions(attributeDescriptions);
		VKVertices ret = new VKVertices();
		ret.createInfo = vi;
		ret.vkVerticiesBuffer = verticesBuf;
		return ret;
	}
}
