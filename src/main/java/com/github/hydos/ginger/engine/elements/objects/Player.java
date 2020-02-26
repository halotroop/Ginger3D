package com.github.hydos.ginger.engine.elements.objects;

import org.lwjgl.glfw.GLFW;

import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.math.vectors.Vector3f;
import com.github.hydos.ginger.engine.render.models.TexturedModel;
import com.github.hydos.ginger.main.settings.Constants;

public class Player extends RenderObject
{
	private double currentSpeed = 0;
	private float currentTurn = 0;
	private float upwardsSpeed = 0;
	private boolean isInAir = false;

	public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale)
	{ super(model, position, rotX, rotY, rotZ, scale); }

	private void checkInputs()
	{
		if (Window.isKeyDown(GLFW.GLFW_KEY_W))
		{
			this.currentSpeed = Constants.movementSpeed;
		}
		else if (Window.isKeyDown(GLFW.GLFW_KEY_S))
		{
			this.currentSpeed = -Constants.movementSpeed;
		}
		else
		{
			this.currentSpeed = 0;
		}
		if (Window.isKeyDown(GLFW.GLFW_KEY_A))
		{
			this.currentTurn = (float) Constants.movementSpeed;
		}
		else if (Window.isKeyDown(GLFW.GLFW_KEY_D))
		{
			this.currentTurn = (float) -Constants.movementSpeed;
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
//		super.increaseRotation(0, (float) ((currentTurn) * Window.getTime()), 0);
		float distance = (float) ((currentSpeed) * (Window.getTime()));
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
		super.increasePosition(dx, 0, dz);
		super.increasePosition(0, (float) (upwardsSpeed * (Window.getTime())), 0);
		upwardsSpeed += Constants.gravity.y() * Window.getTime();
		
		isInAir = false;
		upwardsSpeed = 0;
		super.getPosition().y = 0;
	}
}
