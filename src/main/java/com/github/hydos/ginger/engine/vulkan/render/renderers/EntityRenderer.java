package com.github.hydos.ginger.engine.vulkan.render.renderers;

import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import com.github.hydos.ginger.VulkanExample.VulkanDemoGinger2;
import com.github.hydos.ginger.engine.common.render.Renderer;
import com.github.hydos.ginger.engine.vulkan.VKVariables;

public class EntityRenderer extends Renderer
{
	public EntityRenderer() {
		priority = 1;
	}
	
	@Override
	public void VKRender(MemoryStack stack, VkCommandBuffer commandBuffer, int index) 
	{
        LongBuffer vertexBuffers = stack.longs(VKVariables.vertexBuffer);
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);

        vkCmdBindIndexBuffer(commandBuffer, VKVariables.indexBuffer, 0, VK_INDEX_TYPE_UINT32);

        vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
        	VKVariables.pipelineLayout,
                0, stack.longs(
                	VKVariables.descriptorSets.get(index)
                	), 
                null);

        vkCmdDrawIndexed(commandBuffer, VKVariables.indices.length, 1, 0, 0, 0);
	}
	
	
	
}
