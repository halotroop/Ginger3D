package io.github.hydos.ginger.engine.elements;

import org.lwjgl.glfw.GLFW;

import io.github.hydos.ginger.engine.io.Window;
import io.github.hydos.ginger.engine.mathEngine.vectors.Vector3f;
import io.github.hydos.ginger.engine.renderEngine.models.TexturedModel;
import io.github.hydos.ginger.engine.terrain.Terrain;

public class Player extends Entity{
	
	private static float RUN_SPEED = 0.3f;
	private static float TURN_SPEED = 0.7f;
	public static float GRAVITY = -0.04f;
	private static float JUMP_POWER = 0.3f;
	
	private static float terrainHeight = 0;
	
	private double timeModifier = 1;
	
	private float currentSpeed = 0;
	private float currentTurn = 0;
	private float upwardsSpeed = 0;
	
	private boolean isInAir = false;
	
	public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale, double timeScale) {
		super(model, position, rotX, rotY, rotZ, scale);
		this.timeModifier = timeScale;
	}
	
	public void setSpeeds(float runSpeed, float turnSpeed) {
		RUN_SPEED = runSpeed;
		TURN_SPEED = turnSpeed;
	}
	
	public void setJumpPower(float power) {
		JUMP_POWER = power;
	}

	public void move(Terrain t) {
		checkInputs();
		super.increaseRotation(0, (float) ((currentTurn) * Window.getTime() / timeModifier), 0);
		float distance = (float) ((currentSpeed) * (Window.getTime() / (timeModifier * 2.5)));
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
		super.increasePosition(dx, 0, dz);
		super.increasePosition(0, (float) (upwardsSpeed * (Window.getTime() / (timeModifier * 2))), 0);
		terrainHeight = t.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);
		upwardsSpeed += GRAVITY * Window.getTime() / (timeModifier * 2);
		if(super.getPosition().y < terrainHeight) {
			isInAir = false;
			upwardsSpeed = 0;
			super.getPosition().y = terrainHeight;
		}
		
	}
	
	private void jump() {
		if(!isInAir) {
			isInAir = true;
			this.upwardsSpeed = JUMP_POWER;
		}
	}
	
	private void checkInputs() {
		if(Window.isKeyDown(GLFW.GLFW_KEY_W)) {
			this.currentSpeed = RUN_SPEED;
		}
		else if(Window.isKeyDown(GLFW.GLFW_KEY_S)) {
			this.currentSpeed = -RUN_SPEED;
		}else {
			this.currentSpeed = 0;
		}
		
		if(Window.isKeyDown(GLFW.GLFW_KEY_A)) {
			this.currentTurn = TURN_SPEED;

		}
		else if(Window.isKeyDown(GLFW.GLFW_KEY_D)) {
			this.currentTurn = -TURN_SPEED;
		}
		if(Window.isKeyReleased(68) || Window.isKeyReleased(65)){
			this.currentTurn = 0;
		}
		
		if(Window.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
			jump();
		}else {
			
		}
	}
	
}
