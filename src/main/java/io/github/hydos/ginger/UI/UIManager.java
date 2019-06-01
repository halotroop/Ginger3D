package io.github.hydos.ginger.UI;

import io.github.hydos.ginger.UI.canvases.WelcomeScreen;
import io.github.hydos.ginger.UI.enums.UIColourType;

public class UIManager {
	
	UIColourType colourMode = UIColourType.dark;
	
	UICanvas welcomeScreen;
	
	public UIManager(UIColourType type) {
		this.colourMode = type;
		
		welcomeScreen = new WelcomeScreen();
		
		
	}
	
	public void update() {
		welcomeScreen.update();
	}
	
}
