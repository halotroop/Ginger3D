package com.github.hydos.ginger.engine.common.elements;

import org.joml.Vector3f;

public abstract class RenderObject
{
	
	public Vector3f position;
	public float rotX = 0, rotY = 0, rotZ = 0;
	public Vector3f scale;
	public boolean visible = true;
	
	public void x(float x)
	{ this.position.x = x; }

	public void y(float y)
	{ this.position.y = y; }

	public void z(float z)
	{ this.position.z = z; }
	

	public Vector3f getPosition()
	{ return position; }

	public float getRotX()
	{ return rotX; }

	public float getRotY()
	{ return rotY; }

	public float getRotZ()
	{ return rotZ; }

	public Vector3f getScale()
	{ return scale; }

	public void increasePosition(float dx, float dy, float dz)
	{
		this.position.x += dx;
		this.position.y += dy;
		this.position.z += dz;
	}

	public void increaseRotation(float dx, float dy, float dz)
	{
		this.rotX += dx;
		this.rotY += dy;
		this.rotZ += dz;
	}

	public void setPosition(Vector3f position)
	{ this.position = position; }

	public void setRotX(float rotX)
	{ this.rotX = rotX; }

	public void setRotY(float rotY)
	{ this.rotY = rotY; }

	public void setRotZ(float rotZ)
	{ this.rotZ = rotZ; }

	public void setScale(Vector3f scale)
	{ this.scale = scale; }

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
	
}
