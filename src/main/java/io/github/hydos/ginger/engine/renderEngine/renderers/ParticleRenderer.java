package io.github.hydos.ginger.engine.renderEngine.renderers;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import io.github.hydos.ginger.engine.elements.ThirdPersonCamera;
import io.github.hydos.ginger.engine.mathEngine.Maths;
import io.github.hydos.ginger.engine.mathEngine.matrixes.Matrix4f;
import io.github.hydos.ginger.engine.mathEngine.vectors.Vector3f;
import io.github.hydos.ginger.engine.particle.Particle;
import io.github.hydos.ginger.engine.particle.ParticleTexture;
import io.github.hydos.ginger.engine.renderEngine.models.RawModel;
import io.github.hydos.ginger.engine.renderEngine.shaders.ParticleShader;
import io.github.hydos.ginger.engine.utils.Loader;

public class ParticleRenderer {
	
	private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
	
	private RawModel quad;
	private ParticleShader shader;
	
	public ParticleRenderer(Matrix4f projectionMatrix){
		quad = Loader.loadToVAO(VERTICES, 2);
		shader = new ParticleShader();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}
	
	public void render(Map<ParticleTexture, List<Particle>> particles, ThirdPersonCamera camera){
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		prepare();
		for(ParticleTexture texture : particles.keySet()) {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
			for(Particle particle : particles.get(texture)) {
				updateModelViewMatrix(particle.getPosition(), particle.getRotation(), particle.getScale().x, viewMatrix);
				shader.loadTextureCoordInfo(particle.getTexOffset1(), particle.getTexOffset2(), texture.getNumberOfRows(), particle.getBlend());
				GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
			}
		}

		finishRendering();
	}


	public void cleanUp(){
		shader.cleanUp();
	}
	
	private void updateModelViewMatrix(Vector3f position, float rotation, float scale, Matrix4f viewMatrix) {
		Matrix4f modelMatrix = new Matrix4f();
		Matrix4f.translate(position, modelMatrix, modelMatrix);
		modelMatrix.m00 = viewMatrix.m00;
		modelMatrix.m01 = viewMatrix.m10;
		modelMatrix.m02 = viewMatrix.m20;
		modelMatrix.m10 = viewMatrix.m01;
		modelMatrix.m11 = viewMatrix.m11;
		modelMatrix.m12 = viewMatrix.m21;
		modelMatrix.m20 = viewMatrix.m02;
		modelMatrix.m21 = viewMatrix.m12;
		modelMatrix.m22 = viewMatrix.m22;
		Matrix4f.rotate((float)Math.toRadians(rotation), new Vector3f(0,0,1), modelMatrix, modelMatrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale), modelMatrix, modelMatrix);
		Matrix4f modelViewMatrix = Matrix4f.mul(viewMatrix, modelMatrix, null);
		shader.loadModelViewMatrix(modelViewMatrix);
	}
	
	private void prepare(){
		shader.start();
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);//TODO: add all particle effects into 1 texture to fix overlapping
	}
	
	private void finishRendering(){
		shader.stop();
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
	}

}
