package com.github.hydos.ginger.engine.vulkan.render.renderers;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public abstract class VKRenderer
{
	
	public abstract void render(MemoryStack stack, VkCommandBuffer renderCommandBuffer);
	
}
