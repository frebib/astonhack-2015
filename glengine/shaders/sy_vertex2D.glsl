#version 130 core

uniform mat4 matrixProjection;
uniform mat4 matrixModel;

in vec4 in_Position;
in vec4 in_Color;
in vec2 in_TextureCoord;

out vec4 pass_Color;
out vec2 pass_TextureCoord;

uniform vec4 colour;

void main(void) {
	gl_Position = matrixProjection*matrixModel*in_Position;

	pass_Color = in_Color*colour;
	pass_TextureCoord = in_TextureCoord;
}