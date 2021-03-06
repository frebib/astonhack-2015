#version 130 core

uniform sampler2D texture_diffuse;

#define LIGHT_COUNT 16
uniform vec4 u_light[LIGHT_COUNT];


in vec4 pass_Color;
in vec2 pass_TextureCoord;
in float pass_Depth;
in vec3 pass_Normal;
in vec3 pass_Pos;

out vec4 out_Color;

#define LIGHT_DIR normalize( vec3( 0.25, 0.25, 0.5 ))
#define BRIGHTNESS 0.15
#define CONTRAST 1.25
#define AMBIENT vec4( 32.0/255.0, 32.0/255.0, 32.0/255.0, 1.0 )
#define LIGHT_POS vec3( 450.0, 450.0, 16.0 )
#define DISTANCE  256.0
#define LIGHT_COL vec4( 255.0/255.0, 180.0/255.0, 0.0/255.0, 1.0 )

void main(void) {

	// Light factor
	vec3 normal = normalize(pass_Normal);
	//normal.z = -normal.z;
	
	// Base Colour
	vec4 base = texture(texture_diffuse, pass_TextureCoord);
	if ( base.a*pass_Color.a <= 0.0){ discard; }
	
	vec4 finalCol = vec4(0.0);
	
	// Point Light
	for( int i = 0; i < LIGHT_COUNT; i ++ ){
		if( u_light[i].w > 0.0 ){
			
			vec3 plightDir      = pass_Pos-u_light[i].xyz;
			float dist 			= length(plightDir);
			float attenuation   = 1.0 / (1.0 + (2.0/u_light[i].w)*dist + (2.0/(u_light[i].w*u_light[i].w))*dist*dist);
			//1.0 - clamp(dist/u_light[i].w, 0.0, 1.0); 
			float light_factor  = dot( normal, normalize(plightDir) )*0.8 + 0.2;
			finalCol           += LIGHT_COL * attenuation * light_factor;
		}
	}
	


	// Final Colour
	out_Color   = (AMBIENT+finalCol)*base;//*clamp(finalCol, 0.0, 1.0 );/*light_factor*/;
	out_Color.a = base.a;
	//out_Color = vec4( normal, 1.0);
}