precision mediump float;
#define SHIFT_SIZE 5

uniform sampler2D uTexture;
varying lowp vec2 vTextureCoord;
varying vec4 vBlurTextureCoords[SHIFT_SIZE];
const float weight[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

void main() {
    vec4 centerColor = texture2D(uTexture, vTextureCoord);
    mediump vec3 sum = centerColor.rgb;

    for (int i = 0; i < SHIFT_SIZE; i++) {
        sum += texture2D(uTexture, vBlurTextureCoords[i].xy).rgb * weight[i];
        sum += texture2D(uTexture, vBlurTextureCoords[i].zw).rgb * weight[i];
        sum += texture2D(uTexture, vBlurTextureCoords[i].xw).rgb * weight[i];
        sum += texture2D(uTexture, vBlurTextureCoords[i].zy).rgb * weight[i];
    }
    gl_FragColor = vec4(sum * 1.0 / float(4 * SHIFT_SIZE + 1), 1.0);
}