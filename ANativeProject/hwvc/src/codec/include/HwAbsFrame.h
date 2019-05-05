/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWABSFRAME_H
#define HARDWAREVIDEOCODEC_HWABSFRAME_H

#include "HwMemFrame.h"

class HwAbsFrame : public HwMemFrame {
public:
    enum Type {
        VIDEO,
        AUDIO,
    };

    HwAbsFrame(Type type);

    virtual ~HwAbsFrame();

    void setFormat(uint16_t format);

    uint16_t getFormat();

    Type getType();

    void setPts(int64_t pts);

    int64_t getPts();

    bool isVideo();

    bool isAudio();

    virtual HwAbsFrame *clone()=0;

private:
    Type type = Type::AUDIO;
    uint16_t format = 0;
    int64_t pts;
};


#endif //HARDWAREVIDEOCODEC_HWABSFRAME_H
