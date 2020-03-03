package com.github.hydos.ginger.engine.common.cameras;

import org.joml.Vector3f;
import org.lwjgl.glfw.*;

import com.github.halotroop.litecraft.types.entity.PlayerEntity;
import com.github.hydos.ginger.engine.common.io.Window;

public class Camera
{
	private float distanceFromPlayer = 5;
	private float angleAroundPlayer = 0;
	private Vector3f position = new Vector3f(0, 0, 0);
	private float pitch, yaw;
	private float roll;
	public PlayerEntity playerEntity;

	public Camera(PlayerEntity playerEntity)
	{ this.playerEntity = playerEntity; }

	public Camera(Vector3f vector3f, PlayerEntity playerEntity)
	{
		this.position = vector3f;
		this.playerEntity = playerEntity;
	}

	private void calculateAngleAroundPlayer()
	{
		if (Window.isMouseDown(1))
		{
			float angleChange = (float) (Window.dx * 0.3f);
			angleAroundPlayer -= angleChange;
		}
	}

	private void calculateCameraPosition(float horizDistance, float verticDistance)
	{
		float theta = playerEntity.getRotY() + angleAroundPlayer;
		float offsetX = (float) (horizDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float) (horizDistance * Math.cos(Math.toRadians(theta)));
		position.x = playerEntity.getPosition().x - offsetX;
		position.z = playerEntity.getPosition().z - offsetZ;
		position.y = playerEntity.getPosition().y + verticDistance;
	}

	private float calculateHorizontalDistance()
	{
		float hD = (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
		if (hD < 0)
			hD = 0;
		return hD;
	}

	private void calculatePitch()
	{
		if (Window.isMouseDown(1))
		{
			float pitchChange = (float) (Window.dy * 0.2f);
			pitch += pitchChange;
			if (pitch < 0)
			{
				pitch = 0;
			}
			else if (pitch > 90)
			{ pitch = 90; }
		}
	}

	private float calculateVerticalDistance()
	{ return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch + 4))); }

	private void calculateZoom()
	{
		GLFW.glfwSetScrollCallback(Window.getWindow(), new GLFWScrollCallback()
		{
			@Override
			public void invoke(long win, double dx, double dy)
			{
				float zoomLevel = (float) dy * 0.1f;
				distanceFromPlayer -= zoomLevel;
			}
		});
	}

	public float getPitch()
	{ return pitch; }

	public Vector3f getPosition()
	{ return position; }

	public float getRoll()
	{ return roll; }

	public float getYaw()
	{ return yaw; }

	public void invertPitch()
	{ this.pitch = -pitch; }

	public void updateMovement()
	{
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		this.yaw = 180 - (playerEntity.getRotY() + angleAroundPlayer);
	}

	public void setPitch(float pitch)
	{ this.pitch = pitch; }

	public void setYaw(float yaw)
	{ this.yaw = yaw; }

	public void setRoll(float roll)
	{ this.roll = roll; }
}
