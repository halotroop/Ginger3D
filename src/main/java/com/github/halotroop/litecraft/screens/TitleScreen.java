package com.github.halotroop.litecraft.screens;

import java.util.ArrayList;

import org.joml.*;

import com.github.halotroop.litecraft.Litecraft;
import com.github.halotroop.litecraft.save.LitecraftSave;
import com.github.halotroop.litecraft.world.dimension.Dimensions;
import com.github.hydos.ginger.engine.openGL.api.Ginger;
import com.github.hydos.ginger.engine.openGL.elements.GuiTexture;
import com.github.hydos.ginger.engine.openGL.elements.buttons.TextureButton;
import com.github.hydos.ginger.engine.openGL.font.GUIText;
import com.github.hydos.ginger.engine.openGL.io.Window;
import com.github.hydos.ginger.engine.openGL.screen.Screen;

/**
 * YeS
 */
public class TitleScreen extends Screen
{
	private GUIText debugText;
	private Ginger ginger3D = Ginger.getInstance();
	private TextureButton playButton;
	private Litecraft litecraft = Litecraft.getInstance();

	public TitleScreen()
	{
		elements = new ArrayList<GuiTexture>();
		playButton = ginger3D.registerButton("/textures/guis/playbutton.png", new Vector2f(0, 0), new Vector2f(0.25f, 0.1f));
		playButton.show(Litecraft.getInstance().data.guis);
		debugText = ginger3D.registerText("Loading...", 2, new Vector2f(0, 0), 1f, true, "debugInfo");
		debugText.setBorderWidth(0.5f);
	}

	@Override
	public void render()
	{}

	@Override
	public void tick()
	{
		Vector4i dbg = litecraft.dbgStats;
		debugText.setText("FPS: " + dbg.x() + " UPS: " + dbg.y + " TPS: " + dbg.z);
		playButton.update();
		if (playButton.isClicked())
		{
			Window.lockMouse();
			
			if (Litecraft.getInstance().getWorld() == null)
			{
				Litecraft.getInstance().setSave(new LitecraftSave("cegregatedordinaldata", false));
				Litecraft.getInstance().changeWorld(Litecraft.getInstance().getSave().getWorldOrCreate(Dimensions.OVERWORLD));
				ginger3D.setGingerPlayer(Litecraft.getInstance().getWorld().player);
			}
			if (Litecraft.getInstance().getWorld() != null)
			{
				ginger3D.openScreen(new IngameHUD());
				this.close();
			}
			//TODO: add world creation gui so it takes u to world creation place
			//TODO: add a texture to be rendered behind the gui as an option
		}
	}

	@Override
	public void close()
	{
		this.debugText.remove();
		this.playButton.hide(Litecraft.getInstance().data.guis);
	}
}
