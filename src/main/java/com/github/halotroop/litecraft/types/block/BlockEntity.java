package com.github.halotroop.litecraft.types.block;

import com.github.halotroop.litecraft.world.Chunk;
import com.github.hydos.ginger.engine.elements.objects.RenderObject;
import com.github.hydos.ginger.engine.math.vectors.Vector3f;

public class BlockEntity extends RenderObject
{
	
	
	public BlockEntity(Block block, Vector3f position)
	{ 
		super(block.model, position, 0, 0, 0, new Vector3f(1f, 1f, 1f)); 
	}
	
	public void processCulling(Chunk chunk) {
		Vector3f southNeighbourBlockLocation = this.getPosition();
		southNeighbourBlockLocation.x--;
	}
}
