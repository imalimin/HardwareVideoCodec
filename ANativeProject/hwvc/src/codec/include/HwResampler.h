//
// Created by mingyi.li on 2019/6/16.
//

#ifndef HARDWAREVIDEOCODEC_HWRESAMPLER_H
#define HARDWAREVIDEOCODEC_HWRESAMPLER_H

#include "Object.h"
#include "../include/HwSampleFormat.h"
#include "HwBuffer.h"

#ifdef __cplusplus
extern "C" {
#endif

#include "ff/libswresample/swresample.h"

#ifdef __cplusplus
}
#endif

class HwResampler : public Object {
public:
    HwResampler(HwSampleFormat outFormat, HwSampleFormat inFormat);

    virtual ~HwResampler();

    bool convert(HwBuffer *dest, HwBuffer *src);

private:
    SwrContext *swrContext = nullptr;
    HwSampleFormat inFormat;
    HwSampleFormat outFormat;
};


#endif //HARDWAREVIDEOCODEC_HWRESAMPLER_H
