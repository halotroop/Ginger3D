package com.github.hydos.ginger.engine.vulkan.shaders;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_ALWAYS;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS_OR_EQUAL;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STENCIL_OP_KEEP;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;

import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;

public class Pipeline
{
	public long pipeline;
	public long layout;
	
	public static Pipeline createPipeline(VkDevice device, long renderPass, VkPipelineVertexInputStateCreateInfo vi, long descriptorSetLayout) throws IOException
	{
		int err;
		// Vertex input state
		// Describes the topoloy used with this pipeline
		VkPipelineInputAssemblyStateCreateInfo inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
			.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		// Rasterization state
		VkPipelineRasterizationStateCreateInfo rasterizationState = VkPipelineRasterizationStateCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
			.polygonMode(VK_POLYGON_MODE_FILL)
			.cullMode(VK_CULL_MODE_NONE) // <- VK_CULL_MODE_BACK_BIT would work here, too!
			.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
			.lineWidth(1.0f);
		// Color blend state
		// Describes blend modes and color masks
		VkPipelineColorBlendAttachmentState.Buffer colorWriteMask = VkPipelineColorBlendAttachmentState.calloc(1)
			.colorWriteMask(0xF); // <- RGBA
		VkPipelineColorBlendStateCreateInfo colorBlendState = VkPipelineColorBlendStateCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
			.pAttachments(colorWriteMask);
		// Viewport state
		VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
			.viewportCount(1) // <- one viewport
			.scissorCount(1); // <- one scissor rectangle
		// Enable dynamic states
		// Describes the dynamic states to be used with this pipeline
		// Dynamic states can be set even after the pipeline has been created
		// So there is no need to create new pipelines just for changing
		// a viewport's dimensions or a scissor box
		IntBuffer pDynamicStates = memAllocInt(2);
		pDynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT).put(VK_DYNAMIC_STATE_SCISSOR).flip();
		VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc()
			// The dynamic state properties themselves are stored in the command buffer
			.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
			.pDynamicStates(pDynamicStates);
		// Depth and stencil state
		// Describes depth and stenctil test and compare ops
		VkPipelineDepthStencilStateCreateInfo depthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc()
			// No depth test/write and no stencil used 
			.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
			.depthTestEnable(true)
			.depthWriteEnable(true)
			.depthCompareOp(VK_COMPARE_OP_LESS_OR_EQUAL);
		depthStencilState.back()
			.failOp(VK_STENCIL_OP_KEEP)
			.passOp(VK_STENCIL_OP_KEEP)
			.compareOp(VK_COMPARE_OP_ALWAYS);
		depthStencilState.front(depthStencilState.back());
		// Multi sampling state
		// No multi sampling used in this example
		VkPipelineMultisampleStateCreateInfo multisampleState = VkPipelineMultisampleStateCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
			.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
		// Load shaders
		VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2);
		shaderStages.get(0).set(VKShaderManager.loadShader(device, "/vulkan/shaders/entityVertexShader.glsl", VK_SHADER_STAGE_VERTEX_BIT));
		shaderStages.get(1).set(VKShaderManager.loadShader(device, "/vulkan/shaders/entityFragmentShader.glsl", VK_SHADER_STAGE_FRAGMENT_BIT));
		// Create the pipeline layout that is used to generate the rendering pipelines that
		// are based on this descriptor set layout
		LongBuffer pDescriptorSetLayout = memAllocLong(1).put(0, descriptorSetLayout);
		VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
			.pSetLayouts(pDescriptorSetLayout);
		LongBuffer pPipelineLayout = memAllocLong(1);
		err = vkCreatePipelineLayout(device, pipelineLayoutCreateInfo, null, pPipelineLayout);
		long layout = pPipelineLayout.get(0);
		memFree(pPipelineLayout);
		pipelineLayoutCreateInfo.free();
		memFree(pDescriptorSetLayout);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create pipeline layout: " + VKUtils.translateVulkanResult(err)); }
		// Assign states
		VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1)
			.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
			.layout(layout) // <- the layout used for this pipeline (NEEDS TO BE SET! even though it is basically empty)
			.renderPass(renderPass) // <- renderpass this pipeline is attached to
			.pVertexInputState(vi)
			.pInputAssemblyState(inputAssemblyState)
			.pRasterizationState(rasterizationState)
			.pColorBlendState(colorBlendState)
			.pMultisampleState(multisampleState)
			.pViewportState(viewportState)
			.pDepthStencilState(depthStencilState)
			.pStages(shaderStages)
			.pDynamicState(dynamicState);
		// Create rendering pipeline
		LongBuffer pPipelines = memAllocLong(1);
		err = vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineCreateInfo, null, pPipelines);
		long pipeline = pPipelines.get(0);
		shaderStages.free();
		multisampleState.free();
		depthStencilState.free();
		dynamicState.free();
		memFree(pDynamicStates);
		viewportState.free();
		colorBlendState.free();
		colorWriteMask.free();
		rasterizationState.free();
		inputAssemblyState.free();
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create pipeline: " + VKUtils.translateVulkanResult(err)); }
		com.github.hydos.ginger.engine.vulkan.shaders.Pipeline ret = new com.github.hydos.ginger.engine.vulkan.shaders.Pipeline();
		ret.layout = layout;
		ret.pipeline = pipeline;
		return ret;
	}
}
