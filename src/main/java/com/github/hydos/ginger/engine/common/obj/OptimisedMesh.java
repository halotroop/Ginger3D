package com.github.hydos.ginger.engine.common.obj;

import java.util.*;

import org.joml.*;

/**
 * the ginger 2 standard model (currently only in vulkan) conversion tools will be made later
 * @author hydos
 *
 */
public class OptimisedMesh
{
	
    public List<Vector3f> positions;
    public List<Vector2f> texCoords;
    public List<Integer> indices;

    public OptimisedMesh() {
        this.positions = new ArrayList<>();
        this.texCoords = new ArrayList<>();
        this.indices = new ArrayList<>();
    }
	
}
