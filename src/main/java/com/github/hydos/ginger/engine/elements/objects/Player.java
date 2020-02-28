package com.github.hydos.ginger.engine.elements.objects;

import org.joml.Vector3f;

import com.github.halotroop.litecraft.util.RelativeDirection;
import com.github.hydos.ginger.engine.api.GingerRegister;
import com.github.hydos.ginger.engine.io.Window;
import com.github.hydos.ginger.engine.render.models.TexturedModel;
import com.github.hydos.ginger.main.settings.Constants;

public class Player extends RenderObject
{
	private boolean isInAir = false;
	private double upwardsSpeed;
	private boolean noWeight = true; // because the force of gravity on an object's mass is called WEIGHT, not gravity

	public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale)
	{ super(model, position, rotX, rotY, rotZ, scale); }

	public void move(RelativeDirection direction)
	{
		float ry = (float) Math.toRadians(GingerRegister.getInstance().game.data.camera.getYaw());
		switch (direction)
		{
		case FORWARD:
		default:
			position.z -= Math.cos(ry) * Constants.movementSpeed;
			position.x += Math.sin(ry) * Constants.movementSpeed;
			break;
		case BACKWARD:
			position.z += Math.cos(ry) * Constants.movementSpeed;
			position.x -= Math.sin(ry) * Constants.movementSpeed;
			break;
		case LEFT:
			position.z -= Math.cos(ry + 105) * Constants.movementSpeed;
			position.x += Math.sin(ry + 105) * Constants.movementSpeed;
			break;
		case RIGHT:
			position.z -= Math.cos(ry - 105) * Constants.movementSpeed;
			position.x += Math.sin(ry - 105) * Constants.movementSpeed;
			break;
		case UP:
			if (this.noWeight) position.y += Constants.movementSpeed;
			else this.jump();
			break;
		case DOWN:
			position.y -= Constants.movementSpeed;
			break;
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
		super.increasePosition(0, (float) (upwardsSpeed * (Window.getTime())), 0);
		upwardsSpeed += Constants.gravity.y() * Window.getTime();
		isInAir = false;
		upwardsSpeed = 0;
	}
}
