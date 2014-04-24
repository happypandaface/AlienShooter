#ifdef GL_ES 
precision mediump float;
#endif

varying vec2 v_texCoord;
uniform sampler2D s_texture;
varying vec3 v_normal;

struct SimpleDirectionalLight 
{ 
   vec3 vColor; 
   vec3 vDirection; 
   float fAmbientIntensity; 
}; 

uniform SimpleDirectionalLight sunLight; 
 
void main() {
	vec4 vTexColor = vec4(1.0, 1.0, 1.0, 1.0); 
	float fDiffuseIntensity = max(0.0, dot(normalize(v_normal), -sunLight.vDirection));
	gl_FragColor = texture2D( s_texture, v_texCoord )*(vTexColor*vec4(sunLight.vColor*(sunLight.fAmbientIntensity+fDiffuseIntensity), 1.0));
    
}