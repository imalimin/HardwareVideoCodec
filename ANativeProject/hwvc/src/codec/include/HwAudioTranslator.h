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

#include "ff/libavcodec/avcodec.h"
#include "ff/libavformat/avformat.h"
#include "ff/libavutil/avutil.h"
#include "ff/libswresample/swresample.h"

#ifdef __cplusplus
}
#endif

class HwAudioTranslator : public Object {
public:
    HwAudioTranslator(HwSampleFormat outFormat, HwSampleFormat inFormat);

    virtual ~HwAudioTranslator();

    bool translate(AVFrame **dest, AVFrame **src);

private:
    SwrContext *swrContext = nullptr;
    HwSampleFormat inFormat;
    HwSampleFormat outFormat;
    AVFrame *outFrame = nullptr;
};


#endif //HARDWAREVIDEOCODEC_HWRESAMPLER_H
