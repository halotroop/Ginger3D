package com.github.halotroop.litecraft;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;

public class StarterGL
{
	//	private static final boolean usingEclipse = false;
	public static void main(String[] args)
	{
		System.out.println("GLFW version: " + GLFW.glfwGetVersionString());
		System.out.println("LWJGL version: " + Version.getVersion());
		// Put SoundSystem version here
		new Litecraft();
	}
}
