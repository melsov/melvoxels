uniform mat4 g_WorldViewMatrix;
uniform mat4 g_ProjectionMatrix;

attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec4 inColor;
attribute vec2 inTexCoord;

varying vec2 texCoord;
varying float shadow;
varying float distToCamera;  //TODO get dist to camera: http://stackoverflow.com/questions/16131963/depth-as-distance-to-camera-plane-in-glsl

void main(){
    shadow = inColor.w;

    // Do in two lines (the first and last of the following three) what we did in one before:
    // converting from the vertex's model position to its screen position.
    // In the middle line, put the vertex's distance from the camera into a float to be passed to the frag shader.
    vec4 camRelativePosition = g_WorldViewMatrix * vec4(inPosition, 1.0);
    distToCamera = -camRelativePosition.z;
    gl_Position = g_ProjectionMatrix * camRelativePosition;

    texCoord = inTexCoord;

}