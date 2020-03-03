package com.github.hydos.ginger.engine.common.elements.buttons;

import java.util.List;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import com.github.hydos.ginger.engine.common.elements.GuiTexture;
import com.github.hydos.ginger.engine.common.io.Window;
import com.github.hydos.ginger.engine.opengl.utils.GLLoader;

public class TextureButton
{
	private GuiTexture guiTexture;
	private boolean shown = false;
	private boolean clicked = false;
	private boolean isHovering = false;
	public String resourceLocation;

	public TextureButton(String texture, Vector2f position, Vector2f scale)
	{
		resourceLocation = texture;
		guiTexture = new GuiTexture(GLLoader.loadTextureDirectly(texture), position, scale);
	}

	public void hide(List<GuiTexture> guiTextureList)
	{
		if (!shown)
		{
		}
		else
		{
			guiTextureList.remove(this.guiTexture);
			this.shown = false;
		}
	}

	public boolean isClicked()
	{ return clicked; }

	public boolean isHovering()
	{ return isHovering; }

	public boolean isShown()
	{ return shown; }

	public void show(List<GuiTexture> guiTextureList)
	{
		if (shown)
		{
		}
		else
		{
			guiTextureList.add(this.guiTexture);
			this.shown = true;
		}
	}

	public void update()
	{
		if (shown)
		{
			Vector2f location = guiTexture.getPosition();
			Vector2f scale = guiTexture.getScale();
			Vector2f mouseCoords = Window.getNormalizedMouseCoordinates();
			if (location.y + scale.y > -mouseCoords.y && location.y - scale.y < -mouseCoords.y && location.x + scale.x > mouseCoords.x && location.x - scale.x < mouseCoords.x)
			{
				isHovering = true;
				if (Window.isMousePressed(GLFW.GLFW_MOUSE_BUTTON_1))
				{
					clicked = true;
				}
				else
				{
					clicked = false;
				}
			}
			else
			{
				if (isHovering)
				{ isHovering = false; }
			}
		}
		else
		{
			isHovering = false;
			clicked = false;
		}
	}
}
