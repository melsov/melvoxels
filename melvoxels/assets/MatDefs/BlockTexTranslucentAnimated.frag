varying vec2 texCoord;
varying vec2 texMapOffset;
varying float shadow;

uniform sampler2D m_ColorMap;
uniform vec4 m_Color;

void main(){
    vec4 texColor = texture2D(m_ColorMap, texCoord * .25 + texMapOffset);
    gl_FragColor =  vec4(texColor.rgb, .7) * shadow;
}