package tk.valoeghese.gateways.client.io;

import org.lwjgl.glfw.GLFW;

/*
 * Author: Valoeghese
 */
public final class Keybind
{
	// movement
	public static final Keybind MOVE_FORWARDS = new Keybind(GLFW.GLFW_KEY_W, false);
	public static final Keybind MOVE_LEFT = new Keybind(GLFW.GLFW_KEY_A, false);
	public static final Keybind MOVE_BACKWARDS = new Keybind(GLFW.GLFW_KEY_S, false);
	public static final Keybind MOVE_RIGHT = new Keybind(GLFW.GLFW_KEY_D, false);
	public static final Keybind JUMP = new Keybind(GLFW.GLFW_KEY_SPACE, false);
	public static final Keybind SNEAK = new Keybind(GLFW.GLFW_KEY_LEFT_SHIFT, false);
	// mouse
	public static final Keybind USE = new Keybind(GLFW.GLFW_MOUSE_BUTTON_1, true);
	// hotbar
	public static final Keybind SELECT_0 = new Keybind(GLFW.GLFW_KEY_1, false);
	public static final Keybind SELECT_1 = new Keybind(GLFW.GLFW_KEY_2, false);
	public static final Keybind SELECT_2 = new Keybind(GLFW.GLFW_KEY_3, false);
	// general
	public static final Keybind EXIT = new Keybind(GLFW.GLFW_KEY_ESCAPE, false);
	public static final Keybind FULLSCREEN = new Keybind(GLFW.GLFW_KEY_F11, false);
	// ========================== //
	public int value;
	public boolean mouse;

	public Keybind(int initValue, boolean isMouse)
	{
		this.value = initValue;
		this.mouse = isMouse;
	}

	public boolean isActive()
	{ return mouse ? MouseCallbackHandler.buttons[value] : KeyCallbackHandler.keys[value]; }
}
