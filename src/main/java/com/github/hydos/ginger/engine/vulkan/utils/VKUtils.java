package com.github.hydos.ginger.engine.vulkan.utils;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.util.shaderc.Shaderc.*;
import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.KHRSurface.*;

import java.io.IOException;
import java.nio.*;

import org.lwjgl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.*;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.common.tools.IOUtil;
import com.github.hydos.ginger.engine.vulkan.VKConstants;
import com.github.hydos.ginger.engine.vulkan.registers.VKRegister;
import com.github.hydos.ginger.engine.vulkan.shaders.Pipeline;

/** @author hydos
 *         a util library for Vulkan */
public class VKUtils
{
	
	public static final int VK_FLAGS_NONE = 0;

	public static long startVulkanDebugging(VkInstance instance, int flags, VkDebugReportCallbackEXT callback)
	{
		VkDebugReportCallbackCreateInfoEXT dbgCreateInfo = VkDebugReportCallbackCreateInfoEXT.calloc()
			.sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT)
			.pfnCallback(callback)
			.flags(flags);
		LongBuffer pCallback = memAllocLong(1);
		int err = vkCreateDebugReportCallbackEXT(instance, dbgCreateInfo, null, pCallback);
		long callbackHandle = pCallback.get(0);
		memFree(pCallback);
		dbgCreateInfo.free();
		if (err != VK12.VK_SUCCESS)
		{ throw new AssertionError("Failed to create VkInstance: " + VKUtils.translateVulkanResult(err)); }
		return callbackHandle;
	}
	
	private static int vulkanStageToShaderc(int stage)
	{
		switch (stage)
		{
		case VK10.VK_SHADER_STAGE_VERTEX_BIT:
			return shaderc_vertex_shader;
		case VK10.VK_SHADER_STAGE_FRAGMENT_BIT:
			return shaderc_fragment_shader;
		case NVRayTracing.VK_SHADER_STAGE_RAYGEN_BIT_NV:
			return shaderc_raygen_shader;
		case NVRayTracing.VK_SHADER_STAGE_CLOSEST_HIT_BIT_NV:
			return shaderc_closesthit_shader;
		case NVRayTracing.VK_SHADER_STAGE_MISS_BIT_NV:
			return shaderc_miss_shader;
		case NVRayTracing.VK_SHADER_STAGE_ANY_HIT_BIT_NV:
			return shaderc_anyhit_shader;
		default:
			throw new IllegalArgumentException("Shader stage: " + stage);
		}
	}

	public static ByteBuffer glslToSpirv(String classPath, int vulkanStage) throws IOException
	{
		System.out.println("Converting shader: " + classPath + " to SPIRV");
		ByteBuffer src = IOUtil.ioResourceToByteBuffer(classPath, 1024);
		long compiler = shaderc_compiler_initialize();
		long options = shaderc_compile_options_initialize();
		ShadercIncludeResolve resolver;
		ShadercIncludeResultRelease releaser;
		shaderc_compile_options_set_optimization_level(options, shaderc_optimization_level_performance);
		shaderc_compile_options_set_include_callbacks(options, resolver = new ShadercIncludeResolve()
		{
			public long invoke(long user_data, long requested_source, int type, long requesting_source, long include_depth)
			{
				ShadercIncludeResult res = ShadercIncludeResult.calloc();
				try
				{
					String src = classPath.substring(0, classPath.lastIndexOf('/')) + "/" + memUTF8(requested_source);
					res.content(IOUtil.ioResourceToByteBuffer(src, 1024));
					res.source_name(memUTF8(src));
					return res.address();
				}
				catch (IOException e)
				{
					throw new AssertionError("Failed to resolve include: " + src);
				}
			}
		}, releaser = new ShadercIncludeResultRelease()
		{
			public void invoke(long user_data, long include_result)
			{
				ShadercIncludeResult result = ShadercIncludeResult.create(include_result);
				memFree(result.source_name());
				result.free();
			}
		}, 0L);
		long res;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			res = shaderc_compile_into_spv(compiler, src, vulkanStageToShaderc(vulkanStage),
				stack.UTF8(classPath), stack.UTF8("main"), options);
			if (res == 0L)
				throw new AssertionError("Internal error during compilation!");
		}
		if (shaderc_result_get_compilation_status(res) != shaderc_compilation_status_success)
		{ throw new AssertionError("Shader compilation failed: " + shaderc_result_get_error_message(res)); }
		int size = (int) shaderc_result_get_length(res);
		ByteBuffer resultBytes = BufferUtils.createByteBuffer(size);
		resultBytes.put(shaderc_result_get_bytes(res));
		resultBytes.flip();
		shaderc_compiler_release(res);
		shaderc_compiler_release(compiler);
		releaser.free();
		resolver.free();
		return resultBytes;
	}

	public static String translateVulkanResult(int vulkanResult)
	{
		switch (vulkanResult)
		{
		case VK10.VK_SUCCESS:
			return "Command successfully completed.";
		case VK10.VK_NOT_READY:
			return "A query has not yet been completed.";
		case VK10.VK_TIMEOUT:
			return "A wait operation has timed out.";
		case VK10.VK_INCOMPLETE:
			return "A return array was too small for the result.";
		case KHRSwapchain.VK_SUBOPTIMAL_KHR:
			return "A swapchain no longer matches the surface properties exactly, but can still be used to present to the surface successfully.";
		case VK10.VK_ERROR_OUT_OF_HOST_MEMORY:
			return "A host memory allocation has failed.";
		case VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY:
			return "A device memory allocation has failed.";
		case VK10.VK_ERROR_INITIALIZATION_FAILED:
			return "Initialization of an object could not be completed for implementation-specific reasons.";
		case VK10.VK_ERROR_DEVICE_LOST:
			return "The logical or physical device has been lost.";
		case VK10.VK_ERROR_MEMORY_MAP_FAILED:
			return "Mapping of a memory object has failed.";
		case VK10.VK_ERROR_LAYER_NOT_PRESENT:
			return "A requested layer is not present or could not be loaded.";
		case VK10.VK_ERROR_EXTENSION_NOT_PRESENT:
			return "A requested extension is not supported.";
		case VK10.VK_ERROR_FEATURE_NOT_PRESENT:
			return "A requested feature is not supported.";
		case VK10.VK_ERROR_INCOMPATIBLE_DRIVER:
			return "The requested version of Vulkan is not supported by the driver or is otherwise incompatible for implementation-specific reasons.";
		case VK10.VK_ERROR_TOO_MANY_OBJECTS:
			return "Too many objects of the same type have already been created.";
		case VK10.VK_ERROR_FORMAT_NOT_SUPPORTED:
			return "The requested format is not supported.";
		case VK_ERROR_SURFACE_LOST_KHR:
			return "The window is no longer available.";
		case VK_ERROR_NATIVE_WINDOW_IN_USE_KHR:
			return "The requested window is already connected to a VkSurfaceKHR, or to some other non-Vulkan API.";
		case KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR:
			return "The display used by a swapchain does not use the same presentable image layout, or is incompatible in a way that prevents sharing an" + " image.";
		case EXTDebugReport.VK_ERROR_VALIDATION_FAILED_EXT:
			return "A validation layer found an error.";
		default:
			return String.format("%s [%d]", "Is an unknown vulkan result", Integer.valueOf(vulkanResult));
		}
	}
	
	public static VkCommandBuffer[] initRenderCommandBuffers(VkDevice device, long commandPool, long[] framebuffers, long renderPass, int width, int height,
		Pipeline pipeline, long descriptorSet, long verticesBuf)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			// Create the render command buffers (one command buffer per framebuffer image)
			VkCommandBufferAllocateInfo cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc()
				.sType(VK12.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
				.commandPool(commandPool)
				.level(VK12.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
				.commandBufferCount(framebuffers.length);
			PointerBuffer pCommandBuffer = memAllocPointer(framebuffers.length);
			int err = VK12.vkAllocateCommandBuffers(device, cmdBufAllocateInfo, pCommandBuffer);
			if (err != VK12.VK_SUCCESS)
			{ throw new AssertionError("Failed to create render command buffer: " + VKUtils.translateVulkanResult(err)); }
			VkCommandBuffer[] renderCommandBuffers = new VkCommandBuffer[framebuffers.length];
			for (int i = 0; i < framebuffers.length; i++)
			{ renderCommandBuffers[i] = new VkCommandBuffer(pCommandBuffer.get(i), device); }
			memFree(pCommandBuffer);
			cmdBufAllocateInfo.free();
			// Create the command buffer begin structure
			VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc()
				.sType(VK12.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
			// Specify clear color (cornflower blue)
			VkClearValue.Buffer clearValues = VkClearValue.calloc(2);
			clearValues.get(0).color()
				.float32(0, 100 / 255.0f)
				.float32(1, 149 / 255.0f)
				.float32(2, 237 / 255.0f)
				.float32(3, 1.0f);
			// Specify clear depth-stencil
			clearValues.get(1).depthStencil().depth(1.0f).stencil(0);
			// Specify everything to begin a render pass
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc()
				.sType(VK12.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
				.renderPass(renderPass)
				.pClearValues(clearValues);
			VkRect2D renderArea = renderPassBeginInfo.renderArea();
			renderArea.offset().set(0, 0);
			renderArea.extent().set(width, height);
			for (int i = 0; i < renderCommandBuffers.length; ++i)
			{
				// Set target frame buffer
				renderPassBeginInfo.framebuffer(framebuffers[i]);
				err = VK12.vkBeginCommandBuffer(renderCommandBuffers[i], cmdBufInfo);
				if (err != VK12.VK_SUCCESS)
				{ throw new AssertionError("Failed to begin render command buffer: " + VKUtils.translateVulkanResult(err)); }
				VK12.vkCmdBeginRenderPass(renderCommandBuffers[i], renderPassBeginInfo, VK12.VK_SUBPASS_CONTENTS_INLINE);
				// Update dynamic viewport state
				VkViewport.Buffer viewport = VkViewport.calloc(1)
					.height(height)
					.width(width)
					.minDepth(0.0f)
					.maxDepth(1.0f);
				VK12.vkCmdSetViewport(renderCommandBuffers[i], 0, viewport);
				viewport.free();
				// Update dynamic scissor state
				VkRect2D.Buffer scissor = VkRect2D.calloc(1);
				scissor.extent().set(width, height);
				scissor.offset().set(0, 0);
				VK12.vkCmdSetScissor(renderCommandBuffers[i], 0, scissor);
				scissor.free();
				// Bind descriptor sets describing shader binding points
				LongBuffer descriptorSets = memAllocLong(1).put(0, descriptorSet);
				VK12.vkCmdBindDescriptorSets(renderCommandBuffers[i], VK12.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.layout, 0, descriptorSets, null);
				memFree(descriptorSets);
				// Bind the rendering pipeline (including the shaders)
				VK12.vkCmdBindPipeline(renderCommandBuffers[i], VK12.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.pipeline);
				// Bind triangle vertices
				LongBuffer offsets = memAllocLong(1);
				offsets.put(0, 0L);
				LongBuffer pBuffers = memAllocLong(1);
				pBuffers.put(0, verticesBuf);
	            LongBuffer vertexBuffers = stack.longs(VKRegister.exampleVKModel.vertexBuffer);
				VK12.vkCmdBindVertexBuffers(renderCommandBuffers[i], 0, vertexBuffers, offsets);
				VK12.vkCmdBindIndexBuffer(renderCommandBuffers[i], VKRegister.exampleVKModel.indexBuffer, 0, 3);// 3 = VK_INDEX_TYPE_UINT32
				memFree(pBuffers);
				memFree(offsets);
				// Draw model
//				VK12.vkCmdDraw(renderCommandBuffers[i], 6, 1, 0, 0); old method
	            VK12.vkCmdDrawIndexed(renderCommandBuffers[i], VKRegister.exampleVKModel.mesh.getIndices().length, 1, 0, 0, 0);
				VK12.vkCmdEndRenderPass(renderCommandBuffers[i]);
				err = VK12.vkEndCommandBuffer(renderCommandBuffers[i]);
				if (err != VK12.VK_SUCCESS)
				{ throw new AssertionError("Failed to begin render command buffer: " + VKUtils.translateVulkanResult(err)); }
			}
			renderPassBeginInfo.free();
			clearValues.free();
			cmdBufInfo.free();
			return renderCommandBuffers;
		}
		

	}

	public static void setupVulkanDebugCallback()
	{
		VKConstants.debugCallback = new VkDebugReportCallbackEXT()
		{
			public int invoke(int flags, int objectType, long object, long location, int messageCode, long pLayerPrefix, long pMessage, long pUserData)
			{
				System.err.println("ERROR OCCURED: " + VkDebugReportCallbackEXT.getString(pMessage));
				return 0;
			}
		};
	}
}