//像素化
precision highp float;
varying vec2 vTextureCoord;
uniform float imageWidthFactor;
uniform float imageHeightFactor;
uniform sampler2D uTexture;
uniform float pixel;
void main(){
    vec2 uv  = vTextureCoord.xy;
    float dx = pixel * imageWidthFactor;
    float dy = pixel * imageHeightFactor;
    vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));
    vec3 tc = texture2D(uTexture, coord).xyz;
    gl_FragColor = vec4(tc, 1.0);
}