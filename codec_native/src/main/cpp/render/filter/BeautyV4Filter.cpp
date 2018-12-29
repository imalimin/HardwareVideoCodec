/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/BeautyV4Filter.h"
#include "../include/NormalDrawer.h"
#include "log.h"

static const string VERTEX = SHADER(
        attribute vec4 aPosition;
        attribute vec2 aTextureCoord;
        varying vec2 vTextureCoord;
        void main(){
            gl_Position = aPosition;
            vTextureCoord = aTextureCoord;
        }
);

static const string FRAGMENT = SHADER(
        precision lowp float;
        uniform sampler2D uTexture;
        varying lowp vec2 vTextureCoord;

        uniform float params;
        uniform float distanceNormalizationFactor;
        uniform float brightness;
        uniform highp float texelWidthOffset;
        uniform highp float texelHeightOffset;

        void main(){
            vec3 centralColor;

            float mul_x = texelWidthOffset;
            float mul_y = texelHeightOffset;

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
            float sampleColor;
            float distanceFromCentralColor;
            float gaussianWeight;

            central = texture2D(uTexture, vTextureCoord).g;
            gaussianWeightTotal = 0.2;
            sum = central * 0.2;

            sampleColor = texture2D(uTexture, blurCoordinates0).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates1).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates2).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates3).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates4).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates5).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates6).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates7).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates8).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates9).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates10).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sampleColor = texture2D(uTexture, blurCoordinates11).g;
            distanceFromCentralColor = min(abs(central - sampleColor) * distanceNormalizationFactor, 1.0);
            gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);
            gaussianWeightTotal += gaussianWeight;
            sum += sampleColor * gaussianWeight;

            sum = sum/gaussianWeightTotal;
            centralColor = texture2D(uTexture, vTextureCoord).rgb;
            sampleColor = centralColor.g - sum + 0.5;

            for(int i = 0; i < 5; ++i) {
                if(sampleColor <= 0.5) {
                    sampleColor = sampleColor * sampleColor * 2.0;
                } else {
                    sampleColor = 1.0 - ((1.0 - sampleColor)*(1.0 - sampleColor) * 2.0);
                }
            }

            float aa = 1.0 + pow(sum, 0.3)*0.07;
            vec3 smoothColor = centralColor*aa - vec3(sampleColor)*(aa-1.0);// get smooth color
            smoothColor = clamp(smoothColor,vec3(0.0),vec3(1.0));//make smooth color right

            smoothColor = mix(centralColor, smoothColor, pow(centralColor.g, 0.33));
            smoothColor = mix(centralColor, smoothColor, pow(centralColor.g, 0.39));
            smoothColor = mix(centralColor, smoothColor, params);
            gl_FragColor = vec4(pow(smoothColor, vec3(0.96)),1.0);
            gl_FragColor.rgb = vec3(gl_FragColor.rgb + vec3(brightness));
        }
);

BeautyV4Filter::BeautyV4Filter() {
    name = __func__;
}

BeautyV4Filter::~BeautyV4Filter() {

}

bool BeautyV4Filter::init(int w, int h) {
    if (!Filter::init(w, h))
        return false;
    texelWidthOffset = 1.6f / w;
    texelHeightOffset = 1.6f / h;
    drawer = new NormalDrawer(VERTEX, FRAGMENT);
    texelWidthOffsetLocation = static_cast<GLuint>(drawer->getAttribLocation("texelWidthOffset"));
    texelHeightOffsetLocation = static_cast<GLuint>(drawer->getUniformLocation(
            "texelHeightOffset"));
    paramsLocation = static_cast<GLuint>(drawer->getUniformLocation("params"));
    distanceLocation = static_cast<GLuint>(drawer->getUniformLocation(
            "distanceNormalizationFactor"));
    brightnessLocation = static_cast<GLuint>(drawer->getUniformLocation("brightness"));
    return true;
}

void BeautyV4Filter::bindResources() {
    drawer->setUniform1f(texelWidthOffsetLocation, texelWidthOffset);
    drawer->setUniform1f(texelHeightOffsetLocation, texelHeightOffset);
    drawer->setUniform1f(paramsLocation, params);
    drawer->setUniform1f(distanceLocation, distance);
    drawer->setUniform1f(brightnessLocation, brightness);
}

void BeautyV4Filter::setParam(int key, int value) {
    Filter::setParam(key, value);
    switch (key) {
        case FILTER_BRIGHT: {
            this->brightness = value / 100.0f * 0.2f;
            break;
        }
        case FILTER_SMOOTH: {
            this->params = value / 100.0f * 2.0f;
            break;
        }
        case FILTER_TEXEL_OFFSET: {
            this->distance = value / 100.0f * 10.0f;
            break;
        }
        default:
            break;
    }
}