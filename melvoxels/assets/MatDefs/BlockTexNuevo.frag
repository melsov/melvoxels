uniform sampler2D m_ColorMap;
uniform vec4 m_Color;

varying vec2 texCoord;
varying float shadow;
varying float distToCamera;

void main(){
    vec4 texColor = texture2D(m_ColorMap, texCoord);

    // mix(a, b, j) interpolates between a and b based on j
    // mix texColor with sky blue (1.0, 0.5, 0.3) based on distToCamera squared over 3000.0 (but no more than 2.2)
    // tweak these values to adjust the color, distance and fall-off of the mist
    vec3 distFadeColor = mix(texColor.rgb, vec3(0.4, 0.8, 1.0), min(1.1, pow(distToCamera, 3.0)/3000000.0));

    gl_FragColor = vec4(mix(m_Color.rgb, distFadeColor, texColor.a), 1.0) * shadow; //WANT
}