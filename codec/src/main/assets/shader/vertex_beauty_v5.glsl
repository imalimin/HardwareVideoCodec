#define SHIFT_SIZE 5
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;
varying vec4 vBlurTextureCoord[SHIFT_SIZE];

// 高斯算子左右偏移值，当偏移值为5时，高斯算子为 11 x 11

const highp float texelWidthOffset = 5;
const highp float texelHeightOffset = 5;

void main(){
    gl_Position = aPosition;
    vTextureCoord = aTextureCoord;
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    for (int i = 0; i < SHIFT_SIZE; i++) {
            vBlurTextureCoord[i] = vec4(vTextureCoord.xy - float(i + 1) * singleStepOffset,
                                           vTextureCoord.xy + float(i + 1) * singleStepOffset;
    }
}