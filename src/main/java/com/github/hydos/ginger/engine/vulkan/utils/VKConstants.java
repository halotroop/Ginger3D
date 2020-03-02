package com.github.hydos.ginger.engine.vulkan.utils;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDebugReportCallbackEXT;

public class VKConstants
{
	public static boolean debug = System.getProperty("NDEBUG") == null;
	public static ByteBuffer[] layers =
	{
		MemoryUtil.memUTF8("VK_LAYER_LUNARG_standard_validation"),
	};
	public static VkDebugReportCallbackEXT debugCallback;
	public static final long MAX_UNSIGNED_INT = -1L;
}
