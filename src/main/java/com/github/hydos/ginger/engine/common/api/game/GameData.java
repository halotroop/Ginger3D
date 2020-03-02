package com.github.hydos.ginger.engine.common.api.game;

import java.util.*;

import org.joml.Vector4f;

import com.github.hydos.ginger.engine.common.cameras.Camera;
import com.github.hydos.ginger.engine.common.elements.GuiTexture;
import com.github.hydos.ginger.engine.common.elements.objects.*;

/**
 * Used for storing essential engine game data so main class isn't messy
 * Also in general used with Game Class
 */
public class GameData
{
	public List<GuiTexture> guis;
	public List<RenderObject> entities;
	public List<Light> lights;
	public List<RenderObject> normalMapEntities;
	public Player player;
	public Camera camera;
	public Vector4f clippingPlane;
	public boolean handleGuis = true;
	public int tickSpeed = 20;

	public GameData(Player player, Camera camera, int tickSpeed)
	{
		clippingPlane = new Vector4f(0, -1, 0, 100000);
		guis = new ArrayList<GuiTexture>();
		entities = new ArrayList<RenderObject>();
		lights = new ArrayList<Light>();
		normalMapEntities = new ArrayList<RenderObject>();
		this.player = player;
		this.camera = camera;
		this.tickSpeed = tickSpeed;
	}
}
