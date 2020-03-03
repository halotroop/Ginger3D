package com.github.hydos.ginger.engine.vulkan.render.renderers;

import static org.lwjgl.system.MemoryUtil.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.vulkan.registers.VKRegister;

public class EntityRenderer extends VKRenderer
{

	@Override
	public void render(MemoryStack stack, VkCommandBuffer renderCommandBuffer)
	{
//		//Bind the models buffers
//		LongBuffer offsets = memAllocLong(1);
//		offsets.put(0, 0L);
//        LongBuffer vertexBuffers = stack.longs(VKRegister.exampleVKModel.vertexBuffer);
//		VK12.vkCmdBindVertexBuffers(renderCommandBuffer, 0, vertexBuffers, offsets);
//		VK12.vkCmdBindIndexBuffer(renderCommandBuffer, VKRegister.exampleVKModel.indexBuffer, 0, 3);// 3 = VK_INDEX_TYPE_UINT32
//		memFree(offsets);
//		
//		//Render the texture
//        VK12.vkCmdDrawIndexed(renderCommandBuffer, VKRegister.exampleVKModel.mesh.getIndices().length, 1, 0, 0, 0);
//		FIXME: make master render get instance render with this instead, etc u get the point
	}
	
	public static void tempStaticRender(MemoryStack stack, VkCommandBuffer renderCommandBuffer)
	{
		//Bind the models buffers
		LongBuffer offsets = memAllocLong(1);
		offsets.put(0, 0L);
        LongBuffer vertexBuffers = stack.longs(VKRegister.exampleVKModel.vertexBuffer);
		VK12.vkCmdBindVertexBuffers(renderCommandBuffer, 0, vertexBuffers, offsets);
		VK12.vkCmdBindIndexBuffer(renderCommandBuffer, VKRegister.exampleVKModel.indexBuffer, 0, 0);// 3 = VK_INDEX_TYPE_UINT32
		memFree(offsets);
		
		//Render the texture
        VK12.vkCmdDrawIndexed(renderCommandBuffer, VKRegister.exampleVKModel.mesh.getIndices().length, 1, 0, 0, 0);
	}

	
	
	
}
