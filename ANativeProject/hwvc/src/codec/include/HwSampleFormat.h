//
// Created by mingyi.li on 2019/6/16.
//

#ifndef HARDWAREVIDEOCODEC_HWSAMPLEFORMAT_H
#define HARDWAREVIDEOCODEC_HWSAMPLEFORMAT_H

#include "Object.h"
#include "../include/HwAbsMediaFrame.h"

class HwSampleFormat : public Object {
public:
    HwSampleFormat(HwFrameFormat format, uint16_t channels, uint32_t sampleRate);

    virtual ~HwSampleFormat();

    uint16_t getChannels();

    uint32_t getSampleRate();

    HwFrameFormat getFormat();

private:
    uint16_t channels = 0;
    uint32_t sampleRate = 0;
    HwFrameFormat format = HW_FMT_NONE;
};


#endif //HARDWAREVIDEOCODEC_HWSAMPLEFORMAT_H
