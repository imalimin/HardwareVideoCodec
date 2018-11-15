#define SHIFT_SIZE 5
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;
varying vec4 vBlurTextureCoords[SHIFT_SIZE];

uniform highp float texelWidthOffset;
uniform highp float texelHeightOffset;

void main(){
    gl_Position = aPosition;
    vTextureCoord = aTextureCoord;
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    for (int i = 0; i < SHIFT_SIZE; i++) {
            vBlurTextureCoords[i] = vec4(vTextureCoord.xy - float(i + 1) * singleStepOffset,
                                           vTextureCoord.xy + float(i + 1) * singleStepOffset);
    }
}