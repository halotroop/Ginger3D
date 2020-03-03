package com.github.hydos.ginger.engine.vulkan.memory;

import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

public class VKMemory {
	
	public static boolean getMemoryType(VkPhysicalDeviceMemoryProperties deviceMemoryProperties, int typeBits, int properties, IntBuffer typeIndex)
	{
		int bits = typeBits;
		for (int i = 0; i < 32; i++)
		{
			if ((bits & 1) == 1)
			{
				if ((deviceMemoryProperties.memoryTypes(i).propertyFlags() & properties) == properties)
				{
					typeIndex.put(0, i);
					return true;
				}
			}
			bits >>= 1;
		}
		return false;
	}
	
	
}
