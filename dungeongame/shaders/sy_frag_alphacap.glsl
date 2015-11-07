#version 130 core

uniform sampler2D texture_diffuse;
uniform float grayscale;
uniform float lighting;

in vec4 pass_Color;
in vec2 pass_TextureCoord;
in float pass_Depth;
in vec3 pass_Normal;

out vec4 out_Color;

#define LIGHT_DIR normalize( vec3( 0.25, 0.25, 0.5 ))
#define BRIGHTNESS 0.15
#define CONTRAST 1.25
#define AMBIENT vec4( 240.0/255.0, 220.0/255.0, 240/255.0, 1.0 )

void main(void) {

	// Light factor
	float light_factor = (lighting==1)?BRIGHTNESS+max( 0.0, dot( pass_Normal, LIGHT_DIR ))*CONTRAST:1.0;
	
	// Base Colour
	vec4 base = texture(texture_diffuse, pass_TextureCoord)*mix(vec4(1.0),AMBIENT, (lighting==1)?1.0:0.0);
	
	if ( base.a*pass_Color.a <= 0.25){ discard; }
	
	// Grayscale
	vec4 grayscaleCol = vec4( vec3( (base.r + base.g + base.b)/3.0 ), 1.0 );
	
	// Final Colour
	out_Color = pass_Color*mix( mix( base, grayscaleCol, grayscale )*light_factor, vec4( 0.6, 0.7, 1.0, 1.0), max( 0.0, min( 1.0, pass_Depth)));
	out_Color.a = 1.0;
	//out_Color = vec4( pass_Normal, 1.0);
}