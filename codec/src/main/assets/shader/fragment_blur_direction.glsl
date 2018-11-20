precision mediump float;
#define SHIFT_SIZE 5

uniform sampler2D uTexture;
varying lowp vec2 vTextureCoord;
varying vec4 vBlurTextureCoords[SHIFT_SIZE];

void main() {
    vec4 centerColor = texture2D(uTexture, vTextureCoord);
    mediump vec3 sum = centerColor.rgb;

    for (int i = 0; i < SHIFT_SIZE; i++) {
        sum += texture2D(uTexture, vBlurTextureCoords[i].xy).rgb;
        sum += texture2D(uTexture, vBlurTextureCoords[i].zw).rgb;
        sum += texture2D(uTexture, vBlurTextureCoords[i].xw).rgb;
        sum += texture2D(uTexture, vBlurTextureCoords[i].zy).rgb;
    }
    gl_FragColor = vec4(sum * 1.0 / float(4 * SHIFT_SIZE + 1), 1.0);
}