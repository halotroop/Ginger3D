package com.github.hydos.ginger.engine.vulkan.shaders;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.IOException;
import java.nio.*;

import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.vulkan.utils.VKUtils;

public class VKShaderManager {
	
	public static VkPipelineShaderStageCreateInfo loadShader(VkDevice device, String classPath, int stage) throws IOException
	{
		VkPipelineShaderStageCreateInfo shaderStage = VkPipelineShaderStageCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
			.stage(stage)
			.module(VKShaderManager.loadShader(classPath, device, stage))
			.pName(memUTF8("main"));
		return shaderStage;
	}
	
	public static long loadShader(String classPath, VkDevice device, int stage) throws IOException
	{
		ByteBuffer shaderCode = VKUtils.glslToSpirv(classPath, stage);
		int err;
		VkShaderModuleCreateInfo moduleCreateInfo = VkShaderModuleCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
			.pCode(shaderCode);
		LongBuffer pShaderModule = memAllocLong(1);
		err = vkCreateShaderModule(device, moduleCreateInfo, null, pShaderModule);
		long shaderModule = pShaderModule.get(0);
		memFree(pShaderModule);
		if (err != VK_SUCCESS)
		{ throw new AssertionError("Failed to create shader module: " + VKUtils.translateVulkanResult(err)); }
		return shaderModule;
	}
}
