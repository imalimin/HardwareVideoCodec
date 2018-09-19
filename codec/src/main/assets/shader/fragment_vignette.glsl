uniform sampler2D uTexture;
varying highp vec2 vTextureCoord;

uniform lowp vec2 vignetteCenter;
uniform lowp vec3 vignetteColor;
uniform highp float vignetteStart;
uniform highp float vignetteEnd;

void main(){
    lowp vec3 rgb = texture2D(uTexture, vTextureCoord).rgb;
    lowp float d = distance(vTextureCoord, vec2(vignetteCenter.x, vignetteCenter.y));
    lowp float percent = smoothstep(vignetteStart, vignetteEnd, d);
    gl_FragColor = vec4(mix(rgb.x, vignetteColor.x, percent), mix(rgb.y, vignetteColor.y, percent), mix(rgb.z, vignetteColor.z, percent), 1.0);
}