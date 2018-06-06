precision highp float;

varying vec2 textureCoordinate;
varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;

varying vec2 topTextureCoordinate;
varying vec2 topLeftTextureCoordinate;
varying vec2 topRightTextureCoordinate;

varying vec2 bottomTextureCoordinate;
varying vec2 bottomLeftTextureCoordinate;
varying vec2 bottomRightTextureCoordinate;

uniform sampler2D uTexture;
const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main(){
    float bottomLeftIntensity = dot(texture2D(uTexture, bottomLeftTextureCoordinate).rgb, W);//Grey
    float topRightIntensity = dot(texture2D(uTexture, topRightTextureCoordinate).rgb, W);
    float topLeftIntensity = dot(texture2D(uTexture, topLeftTextureCoordinate).rgb, W);
    float bottomRightIntensity = dot(texture2D(uTexture, bottomRightTextureCoordinate).rgb, W);
    float leftIntensity = dot(texture2D(uTexture, leftTextureCoordinate).rgb, W);
    float rightIntensity = dot(texture2D(uTexture, rightTextureCoordinate).rgb, W);
    float bottomIntensity = dot(texture2D(uTexture, bottomTextureCoordinate).rgb, W);
    float topIntensity = dot(texture2D(uTexture, topTextureCoordinate).rgb, W);
    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;
    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;

    float mag = 1.0 - length(vec2(h, v));

    gl_FragColor = vec4(vec3(mag), 1.0);
}