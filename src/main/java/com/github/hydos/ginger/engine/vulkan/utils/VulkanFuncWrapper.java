package com.github.hydos.ginger.engine.vulkan.utils;

import static org.lwjgl.vulkan.EXTDebugReport.VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT;
import static org.lwjgl.vulkan.KHR8bitStorage.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_8BIT_STORAGE_FEATURES_KHR;
import static org.lwjgl.vulkan.KHRGetMemoryRequirements2.VK_STRUCTURE_TYPE_MEMORY_REQUIREMENTS_2_KHR;
import static org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.*;
import static org.lwjgl.vulkan.KHRShaderFloat16Int8.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FLOAT16_INT8_FEATURES_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.NVRayTracing.*;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.*;
import org.lwjgl.vulkan.*;

public class VulkanFuncWrapper
{
	public static VmaVulkanFunctions VmaVulkanFunctions(MemoryStack stack)
	{ return VmaVulkanFunctions.callocStack(stack); }

	public static VmaAllocatorCreateInfo VmaAllocatorCreateInfo(MemoryStack stack)
	{ return VmaAllocatorCreateInfo.callocStack(stack); }

	public static VkInstanceCreateInfo VkInstanceCreateInfo(MemoryStack stack)
	{ return VkInstanceCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO); }

	public static VkApplicationInfo VkApplicationInfo(MemoryStack stack)
	{ return VkApplicationInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_APPLICATION_INFO); }

	public static VkDebugReportCallbackCreateInfoEXT VkDebugReportCallbackCreateInfoEXT(MemoryStack stack)
	{ return VkDebugReportCallbackCreateInfoEXT.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT); }

	public static VkDeviceCreateInfo VkDeviceCreateInfo(MemoryStack stack)
	{ return VkDeviceCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO); }

	public static VkDeviceQueueCreateInfo.Buffer VkDeviceQueueCreateInfo(MemoryStack stack)
	{ return VkDeviceQueueCreateInfo.callocStack(1, stack).sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO); }

	public static VkPhysicalDevice8BitStorageFeaturesKHR VkPhysicalDevice8BitStorageFeaturesKHR(MemoryStack stack)
	{ return VkPhysicalDevice8BitStorageFeaturesKHR.callocStack(stack).sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_8BIT_STORAGE_FEATURES_KHR); }

	public static VkPhysicalDeviceFloat16Int8FeaturesKHR VkPhysicalDeviceFloat16Int8FeaturesKHR(MemoryStack stack)
	{ return VkPhysicalDeviceFloat16Int8FeaturesKHR.callocStack(stack).sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FLOAT16_INT8_FEATURES_KHR); }

	public static VkPhysicalDeviceProperties2 VkPhysicalDeviceProperties2(MemoryStack stack)
	{ return VkPhysicalDeviceProperties2.callocStack(stack).sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2_KHR); }

	public static VkPhysicalDeviceRayTracingPropertiesNV VkPhysicalDeviceRayTracingPropertiesNV(MemoryStack stack)
	{ return VkPhysicalDeviceRayTracingPropertiesNV.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_RAY_TRACING_PROPERTIES_NV); }

	public static VkSwapchainCreateInfoKHR VkSwapchainCreateInfoKHR(MemoryStack stack)
	{ return VkSwapchainCreateInfoKHR.callocStack(stack).sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR); }

	public static VkImageViewCreateInfo VkImageViewCreateInfo(MemoryStack stack)
	{ return VkImageViewCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO); }

	public static VkCommandPoolCreateInfo VkCommandPoolCreateInfo(MemoryStack stack)
	{ return VkCommandPoolCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO); }

	public static VkMemoryRequirements VkMemoryRequirements(MemoryStack stack)
	{ return VkMemoryRequirements.callocStack(stack); }

	public static VkImageCreateInfo VkImageCreateInfo(MemoryStack stack)
	{ return VkImageCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO); }

	public static VkImageMemoryBarrier.Buffer VkImageMemoryBarrier(MemoryStack stack)
	{ return VkImageMemoryBarrier.callocStack(1, stack).sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER); }

	public static VkFenceCreateInfo VkFenceCreateInfo(MemoryStack stack)
	{ return VkFenceCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO); }

	public static VkSubmitInfo VkSubmitInfo(MemoryStack stack)
	{ return VkSubmitInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_SUBMIT_INFO); }

	public static VkCommandBufferBeginInfo VkCommandBufferBeginInfo(MemoryStack stack)
	{ return VkCommandBufferBeginInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO); }

	public static VkCommandBufferAllocateInfo VkCommandBufferAllocateInfo(MemoryStack stack)
	{ return VkCommandBufferAllocateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO); }

	public static VkMemoryAllocateInfo VkMemoryAllocateInfo(MemoryStack stack)
	{ return VkMemoryAllocateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO); }

	public static VkBufferCreateInfo VkBufferCreateInfo(MemoryStack stack)
	{ return VkBufferCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO); }

	public static VkGeometryAABBNV VkGeometryAABBNV(VkGeometryAABBNV geometry)
	{ return geometry.sType(VK_STRUCTURE_TYPE_GEOMETRY_AABB_NV); }

	public static VkGeometryTrianglesNV VkGeometryTrianglesNV(VkGeometryTrianglesNV geometry)
	{ return geometry.sType(VK_STRUCTURE_TYPE_GEOMETRY_TRIANGLES_NV); }

	public static VkGeometryNV VkGeometryNV(MemoryStack stack)
	{ return VkGeometryNV.callocStack(stack).sType(VK_STRUCTURE_TYPE_GEOMETRY_NV); }

	public static VkMemoryBarrier.Buffer VkMemoryBarrier(MemoryStack stack)
	{ return VkMemoryBarrier.callocStack(1, stack).sType(VK_STRUCTURE_TYPE_MEMORY_BARRIER); }

	public static VkBindAccelerationStructureMemoryInfoNV.Buffer VkBindAccelerationStructureMemoryInfoNV(MemoryStack stack)
	{ return VkBindAccelerationStructureMemoryInfoNV.callocStack(1, stack)
		.sType(VK_STRUCTURE_TYPE_BIND_ACCELERATION_STRUCTURE_MEMORY_INFO_NV); }

	public static VkAccelerationStructureInfoNV VkAccelerationStructureInfoNV(MemoryStack stack)
	{ return VkAccelerationStructureInfoNV.callocStack(stack).sType(VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_INFO_NV); }

	public static VkMemoryRequirements2KHR VkMemoryRequirements2KHR(MemoryStack stack)
	{ return VkMemoryRequirements2KHR.callocStack(stack).sType(VK_STRUCTURE_TYPE_MEMORY_REQUIREMENTS_2_KHR); }

	public static VkAccelerationStructureMemoryRequirementsInfoNV VkAccelerationStructureMemoryRequirementsInfoNV(
		MemoryStack stack)
	{ return VkAccelerationStructureMemoryRequirementsInfoNV.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_INFO_NV); }

	public static VkAccelerationStructureCreateInfoNV VkAccelerationStructureCreateInfoNV(MemoryStack stack)
	{ return VkAccelerationStructureCreateInfoNV.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_CREATE_INFO_NV); }

	public static VkPipelineShaderStageCreateInfo.Buffer VkPipelineShaderStageCreateInfo(MemoryStack stack, int count)
	{
		VkPipelineShaderStageCreateInfo.Buffer ret = VkPipelineShaderStageCreateInfo.callocStack(count, stack);
		ret.forEach(sci -> sci.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO));
		return ret;
	}

	public static VkDescriptorSetLayoutBinding.Buffer VkDescriptorSetLayoutBinding(MemoryStack stack, int count)
	{ return VkDescriptorSetLayoutBinding.callocStack(count, stack); }

	public static VkDescriptorSetLayoutBinding VkDescriptorSetLayoutBinding(MemoryStack stack)
	{ return VkDescriptorSetLayoutBinding.callocStack(stack); }

	public static VkRayTracingPipelineCreateInfoNV.Buffer VkRayTracingPipelineCreateInfoNV(MemoryStack stack)
	{ return VkRayTracingPipelineCreateInfoNV.callocStack(1, stack)
		.sType(VK_STRUCTURE_TYPE_RAY_TRACING_PIPELINE_CREATE_INFO_NV); }

	public static VkRayTracingShaderGroupCreateInfoNV.Buffer VkRayTracingShaderGroupCreateInfoNV(int size, MemoryStack stack)
	{
		VkRayTracingShaderGroupCreateInfoNV.Buffer buf = VkRayTracingShaderGroupCreateInfoNV.callocStack(size, stack);
		buf.forEach(info -> info.sType(VK_STRUCTURE_TYPE_RAY_TRACING_SHADER_GROUP_CREATE_INFO_NV)
			.anyHitShader(VK_SHADER_UNUSED_NV)
			.closestHitShader(VK_SHADER_UNUSED_NV)
			.generalShader(VK_SHADER_UNUSED_NV)
			.intersectionShader(VK_SHADER_UNUSED_NV));
		return buf;
	}

	public static VkPipelineLayoutCreateInfo VkPipelineLayoutCreateInfo(MemoryStack stack)
	{ return VkPipelineLayoutCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO); }

	public static VkDescriptorSetLayoutCreateInfo VkDescriptorSetLayoutCreateInfo(MemoryStack stack)
	{ return VkDescriptorSetLayoutCreateInfo.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO); }

	public static VkDescriptorBufferInfo.Buffer VkDescriptorBufferInfo(MemoryStack stack, int count)
	{ return VkDescriptorBufferInfo.callocStack(count, stack); }

	public static VkDescriptorImageInfo.Buffer VkDescriptorImageInfo(MemoryStack stack, int count)
	{ return VkDescriptorImageInfo.callocStack(count, stack); }

	public static VkDescriptorPoolSize.Buffer VkDescriptorPoolSize(MemoryStack stack, int count)
	{ return VkDescriptorPoolSize.callocStack(count, stack); }

	public static VkWriteDescriptorSetAccelerationStructureNV VkWriteDescriptorSetAccelerationStructureNV(MemoryStack stack)
	{ return VkWriteDescriptorSetAccelerationStructureNV.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET_ACCELERATION_STRUCTURE_NV); }

	public static VkWriteDescriptorSet VkWriteDescriptorSet(MemoryStack stack)
	{ return VkWriteDescriptorSet.callocStack(stack).sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET); }

	public static VkDescriptorSetAllocateInfo VkDescriptorSetAllocateInfo(MemoryStack stack)
	{ return VkDescriptorSetAllocateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO); }

	public static VkDescriptorPoolCreateInfo VkDescriptorPoolCreateInfo(MemoryStack stack)
	{ return VkDescriptorPoolCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO); }

	public static VkPresentInfoKHR VkPresentInfoKHR(MemoryStack stack)
	{ return VkPresentInfoKHR.callocStack(stack).sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR); }

	public static VkSemaphoreCreateInfo VkSemaphoreCreateInfo(MemoryStack stack)
	{ return VkSemaphoreCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO); }

	public static VkQueueFamilyProperties.Buffer VkQueueFamilyProperties(int count)
	{ return VkQueueFamilyProperties.callocStack(count); }

	public static VkPhysicalDeviceFeatures VkPhysicalDeviceFeatures(MemoryStack stack)
	{ return VkPhysicalDeviceFeatures.callocStack(stack); }

	public static VkPhysicalDeviceFeatures2 VkPhysicalDeviceFeatures2(MemoryStack stack)
	{ return VkPhysicalDeviceFeatures2.callocStack(stack).sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2_KHR); }

	public static VkPhysicalDeviceProperties VkPhysicalDeviceProperties(MemoryStack stack)
	{ return VkPhysicalDeviceProperties.callocStack(stack); }

	public static VkGeometryNV.Buffer VkGeometryNV(MemoryStack stack, int count)
	{
		VkGeometryNV.Buffer buf = VkGeometryNV.callocStack(count, stack);
		buf.forEach(info -> info.sType(VK_STRUCTURE_TYPE_GEOMETRY_NV));
		return buf;
	}

	public static VkPipelineShaderStageCreateInfo VkPipelineShaderStageCreateInfo(MemoryStack stack)
	{ return VkPipelineShaderStageCreateInfo.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO); }

	public static VkShaderModuleCreateInfo VkShaderModuleCreateInfo(MemoryStack stack)
	{ return VkShaderModuleCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO); }

	public static VkSurfaceCapabilitiesKHR VkSurfaceCapabilitiesKHR(MemoryStack stack)
	{ return VkSurfaceCapabilitiesKHR.callocStack(stack); }

	public static VkSurfaceFormatKHR.Buffer VkSurfaceFormatKHR(MemoryStack stack, int count)
	{ return VkSurfaceFormatKHR.callocStack(count, stack); }

	public static VmaAllocationCreateInfo VmaAllocationCreateInfo(MemoryStack stack)
	{ return VmaAllocationCreateInfo.callocStack(stack); }

	public static VmaAllocationInfo VmaAllocationInfo(MemoryStack stack)
	{ return VmaAllocationInfo.callocStack(stack); }

	public static VkBufferCopy.Buffer VkBufferCopy(MemoryStack stack, int count)
	{ return VkBufferCopy.callocStack(count, stack); }

	public static VkSamplerCreateInfo VkSamplerCreateInfo(MemoryStack stack)
	{ return VkSamplerCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO); }

	public static VkBufferImageCopy.Buffer VkBufferImageCopy(MemoryStack stack)
	{ return VkBufferImageCopy.callocStack(1, stack); }

	public static VkImageSubresourceRange VkImageSubresourceRange(MemoryStack stack)
	{ return VkImageSubresourceRange.callocStack(stack); }

	public static VkComponentMapping VkComponentMapping(MemoryStack stack)
	{ return VkComponentMapping.callocStack(stack); }

	public static VkAttachmentReference VkAttachmentReference(MemoryStack stack)
	{ return VkAttachmentReference.callocStack(stack); }

	public static VkAttachmentReference.Buffer VkAttachmentReference(MemoryStack stack, int count)
	{ return VkAttachmentReference.callocStack(count, stack); }

	public static VkSubpassDescription.Buffer VkSubpassDescription(MemoryStack stack, int count)
	{ return VkSubpassDescription.callocStack(count, stack); }

	public static VkAttachmentDescription.Buffer VkAttachmentDescription(MemoryStack stack, int count)
	{ return VkAttachmentDescription.callocStack(count, stack); }

	public static VkRenderPassCreateInfo VkRenderPassCreateInfo(MemoryStack stack)
	{ return VkRenderPassCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO); }

	public static VkOffset3D VkOffset3D(MemoryStack stack)
	{ return VkOffset3D.callocStack(stack); }

	public static VkGeometryNV.Buffer VkGeometryNV(int count)
	{ return VkGeometryNV.calloc(count).sType(VK_STRUCTURE_TYPE_GEOMETRY_NV); }

	public static VkFramebufferCreateInfo VkFramebufferCreateInfo(MemoryStack stack)
	{ return VkFramebufferCreateInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO); }

	public static VkPipelineRasterizationStateCreateInfo VkPipelineRasterizationStateCreateInfo(MemoryStack stack)
	{ return VkPipelineRasterizationStateCreateInfo.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO); }

	public static VkPipelineDepthStencilStateCreateInfo VkPipelineDepthStencilStateCreateInfo(MemoryStack stack)
	{ return VkPipelineDepthStencilStateCreateInfo.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO); }

	public static VkPipelineMultisampleStateCreateInfo VkPipelineMultisampleStateCreateInfo(MemoryStack stack)
	{ return VkPipelineMultisampleStateCreateInfo.callocStack(stack)
		.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO); }

	public static VkGraphicsPipelineCreateInfo.Buffer VkGraphicsPipelineCreateInfo(MemoryStack stack, int count)
	{
		VkGraphicsPipelineCreateInfo.Buffer ret = VkGraphicsPipelineCreateInfo.callocStack(count, stack);
		ret.forEach(pci -> pci.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO));
		return ret;
	}

	public static VkClearValue.Buffer VkClearValue(MemoryStack stack, int count)
	{ return VkClearValue.callocStack(count, stack); }

	public static VkRenderPassBeginInfo VkRenderPassBeginInfo(MemoryStack stack)
	{ return VkRenderPassBeginInfo.callocStack(stack).sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO); }

	public static VkViewport.Buffer VkViewport(MemoryStack stack, int count)
	{ return VkViewport.callocStack(count, stack); }

	public static VkFormatProperties VkFormatProperties(MemoryStack stack)
	{ return VkFormatProperties.callocStack(stack); }

	public static VkSubpassDependency.Buffer VkSubpassDependency(MemoryStack stack, int count)
	{ return VkSubpassDependency.callocStack(count, stack); }

	public static VkImageCopy.Buffer VkImageCopy(MemoryStack stack, int count)
	{ return VkImageCopy.callocStack(count, stack); }
}