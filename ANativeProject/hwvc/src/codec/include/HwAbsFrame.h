/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWABSFRAME_H
#define HARDWAREVIDEOCODEC_HWABSFRAME_H

#include "Object.h"

class HwAbsFrame : public Object {
public:
    enum Type {
        VIDEO,
        AUDIO,
    };

    HwAbsFrame(Type type);

    virtual ~HwAbsFrame();

    uint8_t *getData();

    uint64_t getDataSize();

    void setData(uint8_t *data, uint64_t dataSize);

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
    uint8_t *data = nullptr;
    uint64_t dataSize = 0;
    uint16_t format = 0;
    int64_t pts;
};


#endif //HARDWAREVIDEOCODEC_HWABSFRAME_H
