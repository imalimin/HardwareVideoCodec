/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWAUDIOFRAME_H
#define HARDWAREVIDEOCODEC_HWAUDIOFRAME_H

#include "HwAbsMediaFrame.h"

class HwAudioFrame : public HwAbsMediaFrame {
public:
    HwAudioFrame(uint16_t channels, uint32_t sampleRate, uint64_t sampleCount);

    virtual ~HwAudioFrame();

    uint16_t getChannels();

    uint32_t getSampleRate();

    uint64_t getSampleCount();

    void setSampleFormat(uint16_t channels, uint32_t sampleRate, uint64_t sampleCount);

    virtual HwAbsMediaFrame *clone();

    virtual void clone(HwAbsMediaFrame *src);

private:
    uint16_t channels = 0;
    uint32_t sampleRate = 0;
    uint64_t sampleCount = 0;
};


#endif //HARDWAREVIDEOCODEC_HWAUDIOFRAME_H
