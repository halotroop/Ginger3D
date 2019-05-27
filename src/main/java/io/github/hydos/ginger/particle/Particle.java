package io.github.hydos.ginger.particle;

import io.github.hydos.ginger.elements.Player;
import io.github.hydos.ginger.io.Window;
import io.github.hydos.ginger.mathEngine.vectors.Vector3f;

public class Particle {

	private Vector3f position;
	private Vector3f velocity;
	private float gravityEffect;
	private float lifeLength;
	private float rotation;
	private Vector3f scale;
	
	private float elapsedTime = 0;

	public Particle(Vector3f position, Vector3f velocity, float gravityEffect, float lifeLength, float rotation,
			Vector3f scale) {
		super();
		this.position = position;
		this.velocity = velocity;
		this.gravityEffect = gravityEffect;
		this.lifeLength = lifeLength;
		this.rotation = rotation;
		this.scale = scale;
		ParticleMaster.addParticle(this);
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getRotation() {
		return rotation;
	}

	public Vector3f getScale() {
		return scale;
	}
	
	public boolean update() {
		velocity.y += Player.GRAVITY * gravityEffect * Window.getTime();
		Vector3f change = new Vector3f(velocity);
		change.scale((float) Window.getTime());
		Vector3f.add(change, position, position);
		elapsedTime += Window.getTime();
		return elapsedTime < lifeLength;
	}


}
