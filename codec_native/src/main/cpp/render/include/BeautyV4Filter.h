/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_BEAUTYV4FILTER_H
#define HARDWAREVIDEOCODEC_BEAUTYV4FILTER_H

#include "Filter.h"


class BeautyV4Filter : public Filter {
public:
    BeautyV4Filter(int w, int h);

    virtual ~BeautyV4Filter();

    void bindResources() override;

private:
    GLuint texelWidthOffsetLocation = 0;
    GLuint texelHeightOffsetLocation = 0;
    GLuint paramsLocation = 0;
    GLuint distanceLocation = 0;
    GLuint brightnessLocation = 0;

    float texelWidthOffset = 1.0f;
    float texelHeightOffset = 1.0f;
    float params = 1.0f;
    float distance = 7.0f;
    float brightness = 0.015f;
};

#endif //HARDWAREVIDEOCODEC_BEAUTYV4FILTER_H
