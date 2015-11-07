#version 130 core

uniform mat4 matrixProjection;
uniform mat4 matrixView;
uniform mat4 matrixModel;
uniform mat4 matrixModelInverse;
uniform mat4 matrixViewInverse;

in vec4 in_Position;
in vec4 in_Color;
in vec2 in_TextureCoord;
in vec3 in_Normal;

out vec4 pass_Color;
out vec2 pass_TextureCoord;
out float pass_Depth;
out vec3 pass_Normal;

uniform vec4 colour;

#define MAX 1024
#define MIN 768

void main(void) {
	gl_Position = matrixProjection * matrixView * matrixModel * in_Position;
	pass_Depth = (gl_Position.z-MIN)/(MAX+MIN);
	pass_Color = colour;
	pass_TextureCoord = in_TextureCoord;
	mat4 normalMatrix = matrixViewInverse*matrixModelInverse;
	pass_Normal =in_Normal; /*normalize( normalMatrix * vec4(in_Normal, 1.0)).xyz;*/
}