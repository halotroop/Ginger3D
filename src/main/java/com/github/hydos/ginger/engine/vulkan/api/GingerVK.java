package com.github.hydos.ginger.engine.vulkan.api;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;

import com.github.hydos.ginger.engine.common.api.GingerEngine;
import com.github.hydos.ginger.engine.common.api.game.Game;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.common.screen.Screen;

public class GingerVK extends GingerEngine
{
	public GingerVK()
	{
		INSTANCE = this;
	}

	public void end(IntBuffer pWaitDstStageMask, LongBuffer pImageAcquiredSemaphore, LongBuffer pRenderCompleteSemaphore, LongBuffer pSwapchains, PointerBuffer pCommandBuffers, VkSemaphoreCreateInfo semaphoreCreateInfo, VkSubmitInfo submitInfo, VkPresentInfoKHR presentInfo, VkInstance vulkanInstance, long debugCallbackHandle, GLFWFramebufferSizeCallback framebufferSizeCallback, GLFWKeyCallback keyCallback)
	{ 
		MemoryUtil.memFree(pWaitDstStageMask);
		MemoryUtil.memFree(pImageAcquiredSemaphore);
		MemoryUtil.memFree(pRenderCompleteSemaphore);
		MemoryUtil.memFree(pSwapchains);
		MemoryUtil.memFree(pCommandBuffers);
		semaphoreCreateInfo.free();
		submitInfo.free();
		presentInfo.free();
		EXTDebugReport.vkDestroyDebugReportCallbackEXT(vulkanInstance, debugCallbackHandle, null);
		framebufferSizeCallback.free();
		keyCallback.free();
		GLFW.glfwDestroyWindow(Window.getWindow());
		GLFW.glfwTerminate();
	}

	@Override
	public void cleanup()
	{
		// TODO
	}

	@Override
	public void openScreen(Screen screen)
	{
		// TODO 
	}

	@Override
	public void renderOverlays(Game game)
	{
		// TODO
	}

}
