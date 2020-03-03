package com.github.hydos.ginger.engine.vulkan.model;

import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;

public class VKVertices
{
	public long vkVerticiesBuffer;
	public VkPipelineVertexInputStateCreateInfo createInfo;
	public float[] commonVerticies;
}