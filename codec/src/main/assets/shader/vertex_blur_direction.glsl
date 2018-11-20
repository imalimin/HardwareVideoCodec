#define SHIFT_SIZE 5
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;
varying vec4 vBlurTextureCoords[SHIFT_SIZE];

uniform highp float texelWidthOffset;
uniform highp float texelHeightOffset;
uniform int direction;

void main(){
    gl_Position = aPosition;
    vTextureCoord = aTextureCoord;
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    if(direction > 0){
        singleStepOffset.x = 0.0;
    }else{
        singleStepOffset.y = 0.0;
    }
    for (int i = 0; i < SHIFT_SIZE; i++) {
        vBlurTextureCoords[i] = vec4(vTextureCoord.xy - float(i + 1) * singleStepOffset,
                                        vTextureCoord.xy + float(i + 1) * singleStepOffset);
    }
}