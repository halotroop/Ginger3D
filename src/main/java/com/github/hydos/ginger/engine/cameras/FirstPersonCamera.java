package com.github.hydos.ginger.engine.cameras;

import com.github.hydos.ginger.engine.elements.objects.Player;
import com.github.hydos.ginger.engine.math.vectors.Vector3f;

public class FirstPersonCamera extends Camera
{
	private Vector3f position = new Vector3f(0, 0, 0);
	private float pitch, yaw;
	private float roll;

	public FirstPersonCamera(Player player)
	{
		super(player);
	}

	public float getPitch()
	{ return pitch; }

	public Vector3f getPosition()
	{ return position; }

	public float getRoll()
	{ return roll; }

	public float getYaw()
	{ return yaw; }

	public void move()
	{
		position.x = player.getPosition().x;
		position.z = player.getPosition().z;
		position.y = player.getPosition().y;
		
		roll = player.getRotX();
		yaw = -player.getRotY() + 180;
		pitch = player.getRotZ();
		
	}
}
