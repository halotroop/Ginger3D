package io.github.halotroop.litecraft.types.block;

import io.github.hydos.ginger.engine.elements.objects.RenderObject;
import io.github.hydos.ginger.engine.math.vectors.Vector3f;

public class BlockEntity extends RenderObject
{
	public BlockEntity(Block block, Vector3f position)
	{
		super(block.model, position, 0, 0, 0, new Vector3f(1f,1f,1f));
	}
}
