/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/PinkFilter.h"
#include "../include/NormalDrawer.h"

static const string VERTEX = SHADER(
        attribute vec4 aPosition;
        attribute vec2 aTextureCoord;
        varying vec2 vTextureCoord;
        void main(){
            gl_Position= aPosition;
            vTextureCoord = aTextureCoord;
        }
);

static const string FRAGMENT = SHADER(
        precision lowp float;

        varying highp vec2 vTextureCoord;

        uniform sampler2D sTexture;
        uniform sampler2D sTexture2;

        void main()
        {

            highp vec4 textureColor = texture2D(sTexture, vTextureCoord);

            highp float blueColor = textureColor.b * 15.0;

            highp vec2 quad1;
            quad1.y = floor(floor(blueColor) / 4.0);
            quad1.x = floor(blueColor) - (quad1.y * 4.0);

            highp vec2 quad2;
            quad2.y = floor(ceil(blueColor) / 4.0);
            quad2.x = ceil(blueColor) - (quad2.y * 4.0);

            highp vec2 texPos1;
            texPos1.x = (quad1.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
            texPos1.y = (quad1.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

            highp vec2 texPos2;
            texPos2.x = (quad2.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
            texPos2.y = (quad2.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

            lowp vec4 newColor1 = texture2D(sTexture2, texPos1);
            lowp vec4 newColor2 = texture2D(sTexture2, texPos2);

            lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
            gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), 1.0);

        }
);

PinkFilter::PinkFilter(string *names, string *samplers, int size)
        : BaseMultipleSamplerFilter(names, samplers, size) {
    name = __func__;
}

PinkFilter::~PinkFilter() {

}

bool PinkFilter::init(int w, int h) {
    return BaseMultipleSamplerFilter::init(w, h);
}

BaseDrawer *PinkFilter::getDrawer() {
    return new NormalDrawer(VERTEX, FRAGMENT);
}