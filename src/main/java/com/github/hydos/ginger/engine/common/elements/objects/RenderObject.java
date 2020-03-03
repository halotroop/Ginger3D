package com.github.hydos.ginger.engine.common.elements.objects;

import org.joml.Vector3f;

import com.github.hydos.ginger.engine.opengl.render.models.GLTexturedModel;

public class RenderObject
{
	private GLTexturedModel model;
	public Vector3f position;
	private float rotX = 0, rotY = 0, rotZ = 0;
	private Vector3f scale;
	private boolean visible = true;

	public RenderObject(GLTexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale)
	{
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
	}

	public void x(float x)
	{ this.position.x = x; }

	public void y(float y)
	{ this.position.y = y; }

	public void z(float z)
	{ this.position.z = z; }

	public GLTexturedModel getModel()
	{ return model; }

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

	public void setModel(GLTexturedModel model)
	{ this.model = model; }

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
