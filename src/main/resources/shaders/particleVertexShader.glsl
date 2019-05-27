#version 140

in vec2 position;

uniform mat4 modelViewMatrix;
uniform vec4 texOffsets;
uniform float blendFactor;


out vec2 textureCoords1;
out vec2 textureCoords2;
out float blend;

uniform mat4 projectionMatrix;
uniform float nuberOfRows;

void main(void){

	vec2 textureCoords = position + vec2(0.5, 0.5);
	textureCoords.y = 1.0 - textureCoords.y;

	textureCoords /= nuberOfRows;
	textureCoords1 = textureCoords + texOffsets.xy;
	textureCoords2 = textureCoords + texOffsets.zw;
	blend = blendFactor;

	gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 0.0, 1.0);

}
