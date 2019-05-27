package io.github.hydos.ginger.particle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.hydos.ginger.elements.ThirdPersonCamera;
import io.github.hydos.ginger.mathEngine.matrixes.Matrix4f;
import io.github.hydos.ginger.renderEngine.renderers.ParticleRenderer;

public class ParticleMaster {
	
	private static List<Particle> particles = new ArrayList<Particle>();
	private static ParticleRenderer particleRenderer;
	
	public static void init(Matrix4f projectionMatrix) {
		particleRenderer = new ParticleRenderer(projectionMatrix);
		
	}
	
	public static void update() {
		Iterator<Particle> iterator= particles.iterator();
		while(iterator.hasNext()) {
			Particle p = iterator.next();
			boolean stillAlive = p.update();
			if(!stillAlive) {
				iterator.remove();
			}
		}
	}
	
	public static void renderParticles(ThirdPersonCamera camera) {
		particleRenderer.render(particles, camera);
	}
	
	public static void cleanUp() {
		particleRenderer.cleanUp();
	}
	
	public static void addParticle(Particle particle) {
		particles.add(particle);
	}
	
	
	
	
}
