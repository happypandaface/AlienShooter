attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
 
uniform mat4 u_worldTrans;
uniform mat4 u_projTrans;
uniform mat4 normalMatrix;
varying vec2 v_texCoord;
varying vec3 v_normal;
 
void main() {
    gl_Position = u_projTrans * u_worldTrans * vec4(a_position, 1.0);
    v_texCoord = a_texCoord0;
    vec4 vRes = normalMatrix*vec4(a_normal, 0.0);
    v_normal = vRes.xyz;
}