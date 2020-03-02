package com.github.halotroop.litecraft.types.entity;

import org.joml.Vector3f;

import com.github.hydos.ginger.engine.common.elements.objects.RenderObject;
import com.github.hydos.ginger.engine.openGL.render.models.TexturedModel;

public abstract class Entity extends RenderObject
{
	public Entity(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale)
	{ super(model, position, rotX, rotY, rotZ, scale); }
}
