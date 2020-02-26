package com.github.hydos.ginger;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;

import com.github.halotroop.litecraft.Litecraft;

public class Starter
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
