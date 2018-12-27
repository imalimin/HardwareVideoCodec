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

class Image : public Unit {
public:
    Image();

    virtual ~Image();

    bool dispatch(Message *msg) override;

    virtual void release() override;

private:
    JpegDecoder *decoder = nullptr;
    int width = 0, height = 0;
    uint8_t *rgba = nullptr;

    void show(string path);

    bool decode(string path);
};


#endif //HARDWAREVIDEOCODEC_IMAGE_H
