package com.github.hydos.ginger.engine.elements.objects;

import org.lwjgl.glfw.GLFW;

import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.math.vectors.Vector3f;
import com.github.hydos.ginger.engine.render.models.TexturedModel;
import com.github.hydos.ginger.main.settings.Constants;

public class Player extends RenderObject
{
	private boolean isInAir = false;
	private double upwardsSpeed;

	public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale)
	{ super(model, position, rotX, rotY, rotZ, scale); }

	private void checkInputs()
	{
		float ry = getRotY();
		if (Window.isKeyDown(GLFW.GLFW_KEY_W))
		{
			position.z -= Math.cos(ry) * Constants.movementSpeed;
			position.x += Math.sin(ry) * Constants.movementSpeed;
		}
		
		if (Window.isKeyDown(GLFW.GLFW_KEY_A))
		{
			position.z -= Math.cos(ry) * Constants.movementSpeed;
			position.x -= Math.sin(ry) * Constants.movementSpeed;	
		}
		
		if (Window.isKeyDown(GLFW.GLFW_KEY_S))
		{
			position.z += Math.cos(ry) * Constants.movementSpeed;
			position.x -= Math.sin(ry) * Constants.movementSpeed;
		}
		
		if (Window.isKeyDown(GLFW.GLFW_KEY_D))
		{
			position.z += Math.cos(ry) * Constants.movementSpeed;
			position.x += Math.sin(ry) * Constants.movementSpeed;
		}
		
		if (Window.isKeyDown(GLFW.GLFW_KEY_SPACE))
		{
			jump();
		}
	}

	private void jump()
	{
		if (!isInAir)
		{
			isInAir = true;
			this.upwardsSpeed = Constants.jumpPower;
		}
	}

	public void updateMovement()
	{
		checkInputs();
		super.increasePosition(0, (float) (upwardsSpeed * (Window.getTime())), 0);
		upwardsSpeed += Constants.gravity.y() * Window.getTime();
		isInAir = false;
		upwardsSpeed = 0;
		super.getPosition().y = 0;
	}
}
