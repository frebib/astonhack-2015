#version 130 core

uniform sampler2D texture_diffuse;

in vec4 pass_Color;
in vec2 pass_TextureCoord;

out vec4 out_Color;

void main(void) {
	vec4 base = texture(texture_diffuse, pass_TextureCoord);
	out_Color = pass_Color*base;
}