package com.github.hydos.ginger.engine.common.obj;

public class Mesh
{
	public float[] vertices;
	public float[] textureCoords;
	public float[] normals;
	public int[] indices;
	public float furthestPoint;

	public Mesh(float[] vertices, float[] textureCoords, float[] normals, int[] indices,
		float furthestPoint)
	{
		this.vertices = vertices;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.indices = indices;
		this.furthestPoint = furthestPoint;
	}

	public Mesh()
	{}

	public float getFurthestPoint()
	{ return furthestPoint; }

	public int[] getIndices()
	{ return indices; }

	public float[] getNormals()
	{ return normals; }

	public float[] getTextureCoords()
	{ return textureCoords; }

	public float[] getVertices()
	{ return vertices; }
}
