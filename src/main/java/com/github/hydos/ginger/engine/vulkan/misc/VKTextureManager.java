package com.github.hydos.ginger.engine.vulkan.misc;

import static org.lwjgl.stb.STBImage.STBI_rgb_alpha;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_ALWAYS;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.file.Paths;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import com.github.hydos.ginger.VulkanExample;
import com.github.hydos.ginger.engine.vulkan.VKVariables;

public class VKTextureManager
{

	public static void createTextureImage()
	{
		try(MemoryStack stack = stackPush()) {

			String filename = Paths.get(new URI(ClassLoader.getSystemClassLoader().getResource("textures/chalet.jpg").toExternalForm())).toString();

			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			IntBuffer pChannels = stack.mallocInt(1);

			ByteBuffer pixels = stbi_load(filename, pWidth, pHeight, pChannels, STBI_rgb_alpha);

			long imageSize = pWidth.get(0) * pHeight.get(0) * 4; // pChannels.get(0);

			VKVariables.mipLevels = (int) Math.floor(VulkanExample.log2(Math.max(pWidth.get(0), pHeight.get(0)))) + 1;

			if(pixels == null) {
				throw new RuntimeException("Failed to load texture image " + filename);
			}

			LongBuffer pStagingBuffer = stack.mallocLong(1);
			LongBuffer pStagingBufferMemory = stack.mallocLong(1);
			VulkanExample.createBuffer(imageSize,
				VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
				VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
				pStagingBuffer,
				pStagingBufferMemory);


			PointerBuffer data = stack.mallocPointer(1);
			vkMapMemory(VKVariables.device, pStagingBufferMemory.get(0), 0, imageSize, 0, data);
			{
				VulkanExample.memcpy(data.getByteBuffer(0, (int)imageSize), pixels, imageSize);
			}
			vkUnmapMemory(VKVariables.device, pStagingBufferMemory.get(0));

			stbi_image_free(pixels);

			LongBuffer pTextureImage = stack.mallocLong(1);
			LongBuffer pTextureImageMemory = stack.mallocLong(1);
			VulkanExample.createImage(pWidth.get(0), pHeight.get(0),
				VKVariables.mipLevels,
				VK_SAMPLE_COUNT_1_BIT, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_TILING_OPTIMAL,
				VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
				VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
				pTextureImage,
				pTextureImageMemory);

			VKVariables.textureImage = pTextureImage.get(0);
			VKVariables.textureImageMemory = pTextureImageMemory.get(0);

			VulkanExample.transitionImageLayout(VKVariables.textureImage,
				VK_FORMAT_R8G8B8A8_SRGB,
				VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
				VKVariables.mipLevels);

			VulkanExample.copyBufferToImage(pStagingBuffer.get(0), VKVariables.textureImage, pWidth.get(0), pHeight.get(0));

			// Transitioned to VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL while generating mipmaps
			VulkanExample.generateMipmaps(VKVariables.textureImage, VK_FORMAT_R8G8B8A8_SRGB, pWidth.get(0), pHeight.get(0), VKVariables.mipLevels);

			vkDestroyBuffer(VKVariables.device, pStagingBuffer.get(0), null);
			vkFreeMemory(VKVariables.device, pStagingBufferMemory.get(0), null);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static void createTextureImageView() {
		VKVariables.textureImageView = VulkanExample.createImageView(VKVariables.textureImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_ASPECT_COLOR_BIT, VKVariables.mipLevels);
	}
	
	public static void createTextureSampler() {

		try(MemoryStack stack = stackPush()) {

			VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.callocStack(stack);
			samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
			samplerInfo.magFilter(VK_FILTER_LINEAR);
			samplerInfo.minFilter(VK_FILTER_LINEAR);
			samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
			samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
			samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
			samplerInfo.anisotropyEnable(true);
			samplerInfo.maxAnisotropy(16.0f);
			samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
			samplerInfo.unnormalizedCoordinates(false);
			samplerInfo.compareEnable(false);
			samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
			samplerInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
			samplerInfo.minLod(0); // Optional
			samplerInfo.maxLod((float) VKVariables.mipLevels);
			samplerInfo.mipLodBias(0); // Optional

			LongBuffer pTextureSampler = stack.mallocLong(1);

			if(vkCreateSampler(VKVariables.device, samplerInfo, null, pTextureSampler) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create texture sampler");
			}

			VKVariables.textureSampler = pTextureSampler.get(0);
		}
	}
}
