package com.github.hydos.ginger.engine.common.cameras;

import org.joml.Vector3f;

import com.github.hydos.ginger.engine.common.elements.objects.RenderObject;
import com.github.hydos.ginger.engine.common.io.Window;

public class FirstPersonCamera extends Camera
{
	private Vector3f position = new Vector3f(0, 0, 0);
	private float pitch, yaw;
	private float roll;

	public FirstPersonCamera(RenderObject playerEntity)
	{
		this.player = playerEntity;
		playerEntity.setVisible(false);
	}

	@Override
	public float getPitch()
	{ return pitch; }

	@Override
	public Vector3f getPosition()
	{ return position; }

	@Override
	public float getRoll()
	{ return roll; }

	@Override
	public float getYaw()
	{ return yaw; }

	@Override
	public void updateMovement()
	{
		position.x = player.getPosition().x;
		position.z = player.getPosition().z;
		position.y = player.getPosition().y;
		roll = player.getRotX();
		yaw = -player.getRotY() + 180 + Window.getNormalizedMouseCoordinates().x() * 70;
		pitch = player.getRotZ() + -Window.getNormalizedMouseCoordinates().y() * 70;
	}
}
