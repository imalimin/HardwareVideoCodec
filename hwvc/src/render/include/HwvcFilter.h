/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_HWVCFILTER_H
#define HARDWAREVIDEOCODEC_HWVCFILTER_H

#include "Filter.h"
#include "PngDecoder.h"
#include <string>
#include "../tinyxml/FilterReader.h"

using namespace std;

class HwvcFilter : public Filter {
public:
    HwvcFilter(char *path);

    virtual ~HwvcFilter();

    virtual bool init(int w, int h) override;

    void bindResources() override;

private:
    FilterReader *reader = nullptr;
    char **names = nullptr;
    char **samplers = nullptr;
    int size = 0;
    GLuint *textures = nullptr;
    GLint *textureLocations = nullptr;
    PngDecoder *decoder = nullptr;

    GLuint loadTexture(string pngBuf);
};


#endif //HARDWAREVIDEOCODEC_HWVCFILTER_H
