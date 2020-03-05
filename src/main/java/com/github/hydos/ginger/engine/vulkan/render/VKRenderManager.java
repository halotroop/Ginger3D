package com.github.hydos.ginger.engine.vulkan.render;

import java.util.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.github.hydos.ginger.engine.common.exceptions.GingerException;
import com.github.hydos.ginger.engine.common.render.Renderer;

/** used to manage all the renderers and shaders to go with them
 * 
 * @author hydos */
public class VKRenderManager
{
	private static VKRenderManager instance;
	public List<Renderer> renderers;

	public VKRenderManager()
	{ 
		instance = this; 
		renderers = new ArrayList<Renderer>();
	}

	public void addRenderer(Renderer renderer)
	{
		if(renderers == null || renderers.size() == 0)
		{
			renderers = new ArrayList<Renderer>();
			renderers.add(renderer);
		}else {
			for(int i = 0; i < renderers.size(); i++)
			{
				Renderer r = renderers.get(i);
				if(r.priority < renderer.priority) {
					renderers.add(i, renderer);
					return;
				}
			}
		}
	}

	public static VKRenderManager getInstance()
	{
		if (instance == null)
		{ throw new GingerException("The Vulkan render manager is not setup"); }
		return instance;
	}

	public void render(MemoryStack stack, VkCommandBuffer commandBuffer, int index)
	{
		for(Renderer renderer: renderers)
		{
			renderer.VKRender(stack, commandBuffer, index);
		}
	}
}
