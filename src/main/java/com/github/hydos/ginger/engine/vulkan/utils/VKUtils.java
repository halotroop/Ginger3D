package com.github.hydos.ginger.engine.vulkan.utils;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import com.github.hydos.ginger.engine.vulkan.VkConstants;

public class VkUtils
{
	
    private static PointerBuffer getRequiredExtensions() {

        PointerBuffer glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();

        return glfwExtensions;
    }

	
    public static void createInstance() {

        try(MemoryStack stack = MemoryStack.stackPush()) {

            // Use calloc to initialize the structs with 0s. Otherwise, the program can crash due to random values

            VkApplicationInfo appInfo = VkApplicationInfo.callocStack(stack);

            appInfo.sType(VK12.VK_STRUCTURE_TYPE_APPLICATION_INFO);
            appInfo.pApplicationName(stack.UTF8Safe("GingerGame"));
            appInfo.applicationVersion(VK12.VK_MAKE_VERSION(1, 0, 0));
            appInfo.pEngineName(stack.UTF8Safe("Ginger2"));
            appInfo.engineVersion(VK12.VK_MAKE_VERSION(2, 0, 0));
            appInfo.apiVersion(VK12.VK_API_VERSION_1_2);

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.callocStack(stack);

            createInfo.sType(VK12.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
            createInfo.pApplicationInfo(appInfo);
            // enabledExtensionCount is implicitly set when you call ppEnabledExtensionNames
            createInfo.ppEnabledExtensionNames(getRequiredExtensions());

            // We need to retrieve the pointer of the created instance
            PointerBuffer instancePtr = stack.mallocPointer(1);

            if(VK12.vkCreateInstance(createInfo, null, instancePtr) != VK12.VK_SUCCESS) {
                throw new RuntimeException("Failed to create instance");
            }

            VkConstants.vulkanInstance = new VkInstance(instancePtr.get(0), createInfo);
        }
    }
	
	public static void createPhysicalDevice() {
        try(MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer deviceCount = stack.ints(0);

            VK12.vkEnumeratePhysicalDevices(VkConstants.vulkanInstance, deviceCount, null);

            if(deviceCount.get(0) == 0) {
                throw new RuntimeException("Failed to find GPUs with Vulkan support");
            }

            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));

            VK12.vkEnumeratePhysicalDevices(VkConstants.vulkanInstance, deviceCount, ppPhysicalDevices);

            VkPhysicalDevice device = null;

            for(int i = 0;i < ppPhysicalDevices.capacity();i++) {

                device = new VkPhysicalDevice(ppPhysicalDevices.get(i), VkConstants.vulkanInstance);

            }

            if(device == null) {
                throw new RuntimeException("Failed to find a suitable GPU");
            }

            VkConstants.physicalDevice = device;
        }
	}
	
	
    public static class QueueFamilyIndices {

        // We use Integer to use null as the empty value
        private Integer graphicsFamily;
        private Integer presentFamily;

        public boolean isComplete() {
            return graphicsFamily != null && presentFamily != null;
        }

        public int[] unique() {
            return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();
        }
    }
    
    
	public static void createLogicalDevice() 
	{
		try(MemoryStack stack = MemoryStack.stackPush()) 
		{
			QueueFamilyIndices indices = findQueueFamilies();

            int[] uniqueQueueFamilies = indices.unique();

            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.callocStack(uniqueQueueFamilies.length, stack);

            for(int i = 0;i < uniqueQueueFamilies.length;i++) {
                VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i);
                queueCreateInfo.sType(VK12.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
                queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
            }


            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.callocStack(stack);

            createInfo.sType(VK12.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
            createInfo.pQueueCreateInfos(queueCreateInfos);
            // queueCreateInfoCount is automatically set


            PointerBuffer pDevice = stack.pointers(VK12.VK_NULL_HANDLE);

            if(VK12.vkCreateDevice(VkConstants.physicalDevice, createInfo, null, pDevice) != VK12.VK_SUCCESS) {
                throw new RuntimeException("Failed to create logical device");
            }

            VkConstants.device = new VkDevice(pDevice.get(0), VkConstants.physicalDevice, createInfo);

            PointerBuffer pQueue = stack.pointers(VK12.VK_NULL_HANDLE);

            VK12.vkGetDeviceQueue(VkConstants.device, indices.graphicsFamily, 0, pQueue);
            VkConstants.graphicsQueue = new VkQueue(pQueue.get(0), VkConstants.device);

            VK12.vkGetDeviceQueue(VkConstants.device, indices.presentFamily, 0, pQueue);
            VkConstants.presentQueue = new VkQueue(pQueue.get(0), VkConstants.device);
		}
	}
	
    private static QueueFamilyIndices findQueueFamilies() {

        QueueFamilyIndices indices = new QueueFamilyIndices();

        try(MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer queueFamilyCount = stack.ints(0);

            VK12.vkGetPhysicalDeviceQueueFamilyProperties(VkConstants.physicalDevice, queueFamilyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);

            VK12.vkGetPhysicalDeviceQueueFamilyProperties(VkConstants.physicalDevice, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK12.VK_FALSE);

            for(int i = 0;i < queueFamilies.capacity() || !indices.isComplete();i++) {

                if((queueFamilies.get(i).queueFlags() & VK12.VK_QUEUE_GRAPHICS_BIT) != 0) {
                    indices.graphicsFamily = i;
                }

                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(
                	VkConstants.physicalDevice, 
                	i, VkConstants.windowSurface, 
                	presentSupport);

                if(presentSupport.get(0) == VK12.VK_TRUE) {
                    indices.presentFamily = i;
                }
            }

            return indices;
        }
    }
    
    //some code from LWJGL examples for debugging (has changes)
    public static String translateVulkanResult(int result) {
        switch (result) {
        // Success codes
        case VK12.VK_SUCCESS:
            return "Command successfully completed.";
        case VK12.VK_NOT_READY:
            return "A fence or query has not yet completed.";
        case VK12.VK_TIMEOUT:
            return "A wait operation has not completed in the specified time.";
        case VK12.VK_EVENT_SET:
            return "An event is signaled.";
        case VK12.VK_EVENT_RESET:
            return "An event is unsignaled.";
        case VK12.VK_INCOMPLETE:
            return "A return array was too small for the result.";
        case KHRSwapchain.VK_SUBOPTIMAL_KHR:
            return "A swapchain no longer matches the surface properties exactly, but can still be used to present to the surface successfully.";

            // Error codes
        case VK12.VK_ERROR_OUT_OF_HOST_MEMORY:
            return "A host memory allocation has failed.";
        case VK12.VK_ERROR_OUT_OF_DEVICE_MEMORY:
            return "A device memory allocation has failed.";
        case VK12.VK_ERROR_INITIALIZATION_FAILED:
            return "Initialization of an object could not be completed for implementation-specific reasons.";
        case VK12.VK_ERROR_DEVICE_LOST:
            return "The logical or physical device has been lost.";
        case VK12.VK_ERROR_MEMORY_MAP_FAILED:
            return "Mapping of a memory object has failed.";
        case VK12.VK_ERROR_LAYER_NOT_PRESENT:
            return "A requested layer is not present or could not be loaded.";
        case VK12.VK_ERROR_EXTENSION_NOT_PRESENT:
            return "A requested extension is not supported.";
        case VK12.VK_ERROR_FEATURE_NOT_PRESENT:
            return "A requested feature is not supported.";
        case VK12.VK_ERROR_INCOMPATIBLE_DRIVER:
            return "The requested version of Vulkan is not supported by the driver or is otherwise incompatible for implementation-specific reasons.";
        case VK12.VK_ERROR_TOO_MANY_OBJECTS:
            return "Too many objects of the type have already been created.";
        case VK12.VK_ERROR_FORMAT_NOT_SUPPORTED:
            return "A requested format is not supported on this device.";
        case KHRSurface.VK_ERROR_SURFACE_LOST_KHR:
            return "A surface is no longer available.";
        case KHRSurface.VK_ERROR_NATIVE_WINDOW_IN_USE_KHR:
            return "The requested window is already connected to a VkSurfaceKHR, or to some other non-Vulkan API.";
        case KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR:
            return "A surface has changed in such a way that it is no longer compatible with the swapchain, and further presentation requests using the "
                    + "swapchain will fail. Applications must query the new surface properties and recreate their swapchain if they wish to continue" + "presenting to the surface.";
        case KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR:
            return "The display used by a swapchain does not use the same presentable image layout, or is incompatible in a way that prevents sharing an" + " image.";
        case EXTDebugReport.VK_ERROR_VALIDATION_FAILED_EXT:
            return "A validation layer found an error.";
        default:
            return String.format("%s [%d]", "Unknown", Integer.valueOf(result));
        }
    }
	
}
