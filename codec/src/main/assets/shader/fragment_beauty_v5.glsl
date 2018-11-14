precision mediump float;
#define SHIFT_SIZE 5

uniform sampler2D uTexture;
varying lowp vec2 vTextureCoord;
varying vec4 vBlurTextureCoord[SHIFT_SIZE];

void main() {
    vec4 currentColor = texture2D(uTexture, vTextureCoord);
    mediump vec3 sum = currentColor.rgb;

    for (int i = 0; i < SHIFT_SIZE; i++) {
        sum += texture2D(uTexture, vBlurTextureCoord[i].xy).rgb;
        sum += texture2D(uTexture, vBlurTextureCoord[i].zw).rgb;
    }
    // 求出平均值
    gl_FragColor = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);
}