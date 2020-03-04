package com.github.hydos.ginger;

import org.lwjgl.glfw.GLFW;

import com.github.hydos.ginger.engine.common.info.RenderAPI;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.vulkan.api.*;

/** @author hydos06
 *         the non ARR vulkan test example */
public class VulkanStarter
{
    private static class BasicGinger2VulkanExample {

        private static final int WIDTH = 800;
        private static final int HEIGHT = 600;

        // ======= FIELDS ======= //

        private long window;

        // ======= METHODS ======= //

        public void run() {
        	Window.create(WIDTH, HEIGHT, "V u l k a n", 60, RenderAPI.Vulkan);
            initVulkan();
            mainLoop();
            cleanup();
        }

        private void initVulkan() {
        	new GingerVK().start("Vulkan demo");
        }

        private void mainLoop() {

            while(!Window.closed()) {
                if(Window.shouldRender()) {
                	Window.update();
                }
            }

        }

        private void cleanup() {

        	GLFW.glfwDestroyWindow(window);

            GLFW.glfwTerminate();
        }

    }

    public static void main(String[] args) {

        BasicGinger2VulkanExample app = new BasicGinger2VulkanExample();

        app.run();
    }
}