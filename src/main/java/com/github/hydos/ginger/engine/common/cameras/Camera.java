package com.github.hydos.ginger.engine.common.cameras;

import org.joml.Vector3f;

import com.github.hydos.ginger.engine.common.elements.objects.RenderObject;

public abstract class Camera
{
	public RenderObject player;
	private float pitch, yaw, roll;
	private Vector3f position = new Vector3f(0, 0, 0);

	public Vector3f getPosition()
	{ return position; }
	
	public float getPitch()
	{ return pitch; }

	public float getRoll()
	{ return roll; }

	public float getYaw()
	{ return yaw; }

	public void invertPitch()
	{ this.pitch = -pitch; }

	public void setPitch(float pitch)
	{ this.pitch = pitch; }

	public void setYaw(float yaw)
	{ this.yaw = yaw; }

	public void setRoll(float roll)
	{ this.roll = roll; }

	public abstract void updateMovement();
}
