package com.github.hydos.ginger.engine.common.cameras;

import org.joml.Vector3f;

import com.github.halotroop.litecraft.types.entity.PlayerEntity;
import com.github.hydos.ginger.engine.common.io.Window;

public class FirstPersonCamera extends Camera
{
	private Vector3f position = new Vector3f(0, 0, 0);
	private float pitch, yaw;
	private float roll;

	public FirstPersonCamera(PlayerEntity playerEntity)
	{
		super(playerEntity);
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
		position.x = playerEntity.getPosition().x;
		position.z = playerEntity.getPosition().z;
		position.y = playerEntity.getPosition().y;
		roll = playerEntity.getRotX();
		yaw = -playerEntity.getRotY() + 180 + Window.getNormalizedMouseCoordinates().x() * 70;
		pitch = playerEntity.getRotZ() + -Window.getNormalizedMouseCoordinates().y() * 70;
	}
}
