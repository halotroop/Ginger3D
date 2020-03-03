package com.github.hydos.ginger.engine.vulkan.model;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.common.obj.*;
import com.github.hydos.ginger.engine.vulkan.registers.VKRegister;

public class VKModelData
{
	
	public Mesh mesh;
	public long vertexBuffer;
	public long vertexBufferMemory;
	public long indexBuffer;
	public long indexBufferMemory;
	
	public void loadModel() {
        this.mesh = ModelLoader.getCubeMesh();
        createVertexBuffer();
        createIndexBuffer();
    }
	
    public int findMemoryType(int typeFilter, int properties) {

        VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.mallocStack();
        VK12.vkGetPhysicalDeviceMemoryProperties(VKRegister.physicalDevice, memProperties);

        for(int i = 0;i < memProperties.memoryTypeCount();i++) {
            if((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                return i;
            }
        }

        throw new RuntimeException("Failed to find suitable memory type");
    }
	
    public void createBuffer(long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory) {

        try(MemoryStack stack = stackPush()) {

            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack);
            bufferInfo.sType(VK12.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferInfo.size(size);
            bufferInfo.usage(usage);
            bufferInfo.sharingMode(VK12.VK_SHARING_MODE_EXCLUSIVE);

            if(VK12.vkCreateBuffer(VKRegister.device, bufferInfo, null, pBuffer) != VK12.VK_SUCCESS) {
                throw new RuntimeException("Failed to create vertex buffer");
            }

            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            VK12.vkGetBufferMemoryRequirements(VKRegister.device, pBuffer.get(0), memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType(VK12.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), properties));

            if(VK12.vkAllocateMemory(VKRegister.device, allocInfo, null, pBufferMemory) != VK12.VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate vertex buffer memory");
            }

            VK12.vkBindBufferMemory(VKRegister.device, pBuffer.get(0), pBufferMemory.get(0), 0);
        }
    }
	
    private void endSingleTimeCommands(VkCommandBuffer commandBuffer) {

        try(MemoryStack stack = stackPush()) {

            VK12.vkEndCommandBuffer(commandBuffer);

            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.callocStack(1, stack);
            submitInfo.sType(VK12.VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(commandBuffer));

            VK12.vkQueueSubmit(VKRegister.queue, submitInfo, VK12.VK_NULL_HANDLE);
            VK12.vkQueueWaitIdle(VKRegister.queue);

            VK12.vkFreeCommandBuffers(VKRegister.device, VKRegister.commandPool, commandBuffer);
        }
    }
    
    private VkCommandBuffer beginSingleTimeCommands() {

        try(MemoryStack stack = stackPush()) {

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack);
            allocInfo.sType(VK12.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.level(VK12.VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(VKRegister.commandPool);
            allocInfo.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            VK12.vkAllocateCommandBuffers(VKRegister.device, allocInfo, pCommandBuffer);
            VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), VKRegister.device);

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK12.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK12.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            VK12.vkBeginCommandBuffer(commandBuffer, beginInfo);

            return commandBuffer;
        }
    }
	
    private void copyBuffer(long srcBuffer, long dstBuffer, long size) {

        try(MemoryStack stack = stackPush()) {

            VkCommandBuffer commandBuffer = beginSingleTimeCommands();

            VkBufferCopy.Buffer copyRegion = VkBufferCopy.callocStack(1, stack);
            copyRegion.size(size);

            VK12.vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);

            endSingleTimeCommands(commandBuffer);
        }
    }

    
    private void memcpy(ByteBuffer buffer, float[] indices) {

        for(float index : indices) {
            buffer.putFloat(index);
        }

        buffer.rewind();
    }
	
    private void createVertexBuffer() {

        try(MemoryStack stack = stackPush()) {

            long bufferSize = ((3 + 3 + 2) * Float.BYTES) * mesh.getVertices().length;

            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);
            createBuffer(bufferSize,
                    VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK12.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK12.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pBuffer,
                    pBufferMemory);

            long stagingBuffer = pBuffer.get(0);
            long stagingBufferMemory = pBufferMemory.get(0);

            PointerBuffer data = stack.mallocPointer(1);

            VK12.vkMapMemory(VKRegister.device, stagingBufferMemory, 0, bufferSize, 0, data);
            {
                memcpy(data.getByteBuffer(0, (int) bufferSize), mesh.getVertices());
            }
            VK12.vkUnmapMemory(VKRegister.device, stagingBufferMemory);

            createBuffer(bufferSize,
            	VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK12.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            	VK12.VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory);

            vertexBuffer = pBuffer.get(0);
            vertexBufferMemory = pBufferMemory.get(0);

            copyBuffer(stagingBuffer, vertexBuffer, bufferSize);

            VK12.vkDestroyBuffer(VKRegister.device, stagingBuffer, null);
            VK12.vkFreeMemory(VKRegister.device, stagingBufferMemory, null);
        }
    }
    
    private void memcpyIndices(ByteBuffer buffer, int[] indices) {

        for(int index : indices) {
            buffer.putInt(index);
        }

        buffer.rewind();
    }
    
    private void createIndexBuffer() {

        try(MemoryStack stack = stackPush()) {

            long bufferSize = Integer.BYTES * mesh.getIndices().length;

            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);
            createBuffer(bufferSize,
                    VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK12.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK12.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pBuffer,
                    pBufferMemory);

            long stagingBuffer = pBuffer.get(0);
            long stagingBufferMemory = pBufferMemory.get(0);

            PointerBuffer data = stack.mallocPointer(1);

            VK12.vkMapMemory(VKRegister.device, stagingBufferMemory, 0, bufferSize, 0, data);
            {
            	memcpyIndices(data.getByteBuffer(0, (int) bufferSize), mesh.getIndices());
            }
            VK12.vkUnmapMemory(VKRegister.device, stagingBufferMemory);

            createBuffer(bufferSize,
            	VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK12.VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                    VK12.VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory);

            indexBuffer = pBuffer.get(0);
            indexBufferMemory = pBufferMemory.get(0);

            copyBuffer(stagingBuffer, indexBuffer, bufferSize);

            VK12.vkDestroyBuffer(VKRegister.device, stagingBuffer, null);
            VK12.vkFreeMemory(VKRegister.device, stagingBufferMemory, null);
        }
    }
	
}
