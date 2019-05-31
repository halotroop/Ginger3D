package io.github.hydos.ginger.engine.guis.buttons;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import io.github.hydos.ginger.engine.guis.GuiTexture;
import io.github.hydos.ginger.engine.io.Window;
import io.github.hydos.ginger.engine.mathEngine.vectors.Vector2f;
import io.github.hydos.ginger.engine.utils.Loader;

public class Button{
	
	private GuiTexture guiTexture;
				
	private boolean shown = false;
	
	private boolean clicked = false;
	
	private boolean isHovering = false;

	public Button(String texture, Vector2f position, Vector2f scale) {
		guiTexture = new GuiTexture(Loader.loadTextureDirectly(texture), position, scale);
	}
	
	public void update() {
		if(shown) {
			Vector2f location = guiTexture.getPosition();
			Vector2f scale = guiTexture.getScale();
			
			Vector2f mouseCoords = Window.getNormalizedMouseCoordinates();
			System.out.println(mouseCoords);
			if(location.y + scale.y > -mouseCoords.y && location.y - scale.y < -mouseCoords.y && location.x + scale.x > mouseCoords.x && location.x - scale.x < mouseCoords.x) {
				System.out.println("hover");
				isHovering = true;
				if(Window.isMouseDown(GLFW.GLFW_MOUSE_BUTTON_1)) {
					clicked = true;
				}else {
					clicked = false;
				}
				
			}else {
				System.out.println("no hover");
				if(isHovering) {
					isHovering = false;
				}
			}
			
		}
	}
	
	
	public void show(List<GuiTexture> guiTexture) {
		if(shown) {
			
		}else {
			guiTexture.add(this.guiTexture);
			this.shown = true;
		}
		
	}
	
	public void hide(List<GuiTexture> guiTexture) {
		if(!shown) {
			
		}else {
			guiTexture.remove(this.guiTexture);
			this.shown = false;
			
		}
	}
	
	public boolean isShown() {
		return shown;
	}
	
	public boolean isClicked() {
		return clicked;
	}
	
	public boolean isHovering() {
		return isHovering;
	}
	
	
	
}
