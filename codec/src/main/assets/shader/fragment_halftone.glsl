varying highp vec2 vTextureCoord;

uniform sampler2D uTexture;

uniform highp float fractionalWidthOfPixel;
uniform highp float aspectRatio;

const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main(){
  highp vec2 sampleDivisor = vec2(fractionalWidthOfPixel, fractionalWidthOfPixel / aspectRatio);
  highp vec2 samplePos = vTextureCoord - mod(vTextureCoord, sampleDivisor) + 0.5 * sampleDivisor;
  highp vec2 vTextureCoordToUse = vec2(vTextureCoord.x, (vTextureCoord.y * aspectRatio + 0.5 - 0.5 * aspectRatio));
  highp vec2 adjustedSamplePos = vec2(samplePos.x, (samplePos.y * aspectRatio + 0.5 - 0.5 * aspectRatio));
  highp float distanceFromSamplePoint = distance(adjustedSamplePos, vTextureCoordToUse);
  lowp vec3 sampledColor = texture2D(uTexture, samplePos).rgb;
  highp float dotScaling = 1.0 - dot(sampledColor, W);
  lowp float checkForPresenceWithinDot = 1.0 - step(distanceFromSamplePoint, (fractionalWidthOfPixel * 0.5) * dotScaling);
  gl_FragColor = vec4(vec3(checkForPresenceWithinDot), 1.0);
}