#define x_a 640.0
#define y_a 1136.0


precision lowp float;
uniform sampler2D uTexture;
varying lowp vec2 vTextureCoord;

uniform float params;
uniform float distanceNormalizationFactor;
uniform float brightness;

void main(){
    vec3 centralColor;

    float mul_x = 1.6 / x_a;
    float mul_y = 1.6 / y_a;

    vec2 blurCoordinates0 = vTextureCoord + vec2(0.0 * mul_x,-10.0 * mul_y);
    vec2 blurCoordinates1 = vTextureCoord + vec2(5.0 * mul_x,-8.0 * mul_y);
    vec2 blurCoordinates2 = vTextureCoord + vec2(8.0 * mul_x,-5.0 * mul_y);
    vec2 blurCoordinates3 = vTextureCoord + vec2(10.0 * mul_x,0.0 * mul_y);
    vec2 blurCoordinates4 = vTextureCoord + vec2(8.0 * mul_x,5.0 * mul_y);
    vec2 blurCoordinates5 = vTextureCoord + vec2(5.0 * mul_x,8.0 * mul_y);
    vec2 blurCoordinates6 = vTextureCoord + vec2(0.0 * mul_x,10.0 * mul_y);
    vec2 blurCoordinates7 = vTextureCoord + vec2(-5.0 * mul_x,8.0 * mul_y);
    vec2 blurCoordinates8 = vTextureCoord + vec2(-8.0 * mul_x,5.0 * mul_y);
    vec2 blurCoordinates9 = vTextureCoord + vec2(-10.0 * mul_x,0.0 * mul_y);
    vec2 blurCoordinates10 = vTextureCoord + vec2(-8.0 * mul_x,-5.0 * mul_y);
    vec2 blurCoordinates11 = vTextureCoord + vec2(-5.0 * mul_x,-8.0 * mul_y);


    float central;
    float gaussianWeightTotal;
    float sum;
    float sample;
    float distanceFromCentralColor;
    float gaussianWeight;

    central = texture2D(uTexture, vTextureCoord).g;
    gaussianWeightTotal = 0.2;
    sum = central * 0.2;

    sample = texture2D(uTexture, blurCoordinates0).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates1).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates2).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates3).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates4).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates5).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates6).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates7).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates8).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates9).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates10).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sample = texture2D(uTexture, blurCoordinates11).g;
    distanceFromCentralColor = min(abs(central - sample) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += sample * gaussianWeight;

    sum = sum/gaussianWeightTotal;
    centralColor = texture2D(uTexture, vTextureCoord).rgb;
    sample = centralColor.g - sum + 0.5;

    for(int i = 0; i < 5; ++i) {
        if(sample <= 0.5) {
            sample = sample * sample * 2.0;
        } else {
            sample = 1.0 - ((1.0 - sample)*(1.0 - sample) * 2.0);
        }
    }

    float aa = 1.0 + pow(sum, 0.3)*0.07;
    vec3 smoothColor = centralColor*aa - vec3(sample)*(aa-1.0);// get smooth color
    smoothColor = clamp(smoothColor,vec3(0.0),vec3(1.0));//make smooth color right

    smoothColor = mix(centralColor, smoothColor, pow(centralColor.g, 0.33));
    smoothColor = mix(centralColor, smoothColor, pow(centralColor.g, 0.39));
    smoothColor = mix(centralColor, smoothColor, params);
    gl_FragColor = vec4(pow(smoothColor, vec3(0.96)),1.0);
    gl_FragColor.rgb = vec3(gl_FragColor.rgb + vec3(brightness));
}