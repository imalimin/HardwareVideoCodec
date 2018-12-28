/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_IMAGE_H
#define HARDWAREVIDEOCODEC_IMAGE_H

#include "Unit.h"
#include "BaseDrawer.h"
#include "JpegDecoder.h"
#include "PngDecoder.h"
#include "TextureCenter.h"

class Image : public Unit {
public:
    Image();

    virtual ~Image();

    virtual void release() override;

    bool eventPrepare(Message *msg);

    bool eventShow(Message *msg);

private:
    JpegDecoder *decoder = nullptr;
    PngDecoder *pDecoder = nullptr;
    TextureCenter *texCenter = nullptr;
    int width = 0, height = 0;
    uint8_t *rgba = nullptr;

    void show(string path);

    bool decode(string path);
};


#endif //HARDWAREVIDEOCODEC_IMAGE_H
