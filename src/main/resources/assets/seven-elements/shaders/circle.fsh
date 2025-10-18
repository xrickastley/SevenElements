#version 150

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {	
	if (length(texCoord0 - vec2(0.5)) > 0.5) discard;

	fragColor = vertexColor;
}