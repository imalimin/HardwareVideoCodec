/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWVIDEOFRAME_H
#define HARDWAREVIDEOCODEC_HWVIDEOFRAME_H

#include "HwAbsMediaFrame.h"

class HwVideoFrame : public HwAbsMediaFrame {
public:
    HwVideoFrame(uint32_t width, uint32_t height);

    virtual ~HwVideoFrame();

    void setWidth(uint32_t width);

    void setHeight(uint32_t height);

    uint32_t getWidth();

    uint32_t getHeight();

    virtual HwAbsMediaFrame *clone();

    virtual void clone(HwAbsMediaFrame *src);

private:
    uint32_t width = 0;
    uint32_t height = 0;
};


#endif //HARDWAREVIDEOCODEC_HWVIDEOFRAME_H
