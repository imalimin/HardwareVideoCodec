#define SHIFT_SIZE 5
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;
varying vec4 vBlurTextureCoord[SHIFT_SIZE];

const highp float texelWidthOffset = 1.0;
const highp float texelHeightOffset = 1.0;

void main(){
    gl_Position = aPosition;
    vTextureCoord = aTextureCoord;
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    for (int i = 0; i < SHIFT_SIZE; i++) {
            vBlurTextureCoord[i] = vec4(vTextureCoord.xy - float(i + 1) * singleStepOffset,
                                           vTextureCoord.xy + float(i + 1) * singleStepOffset);
    }
}