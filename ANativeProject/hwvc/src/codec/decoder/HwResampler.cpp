//
// Created by mingyi.li on 2019/6/16.
//

#include "../include/HwResampler.h"
#include "Logcat.h"

HwResampler::HwResampler(HwSampleFormat outFormat, HwSampleFormat inFormat)
        : outFormat(outFormat),
          inFormat(inFormat),
          Object() {
    swrContext = swr_alloc_set_opts(swrContext,
                                    outFormat.getChannels(),
                                    HwAbsMediaFrame::convertAudioFrameFormat(outFormat.getFormat()),
                                    outFormat.getSampleRate(),
                                    inFormat.getChannels(),
                                    HwAbsMediaFrame::convertAudioFrameFormat(inFormat.getFormat()),
                                    inFormat.getSampleRate(),
                                    0, nullptr);
    if (!swrContext || 0 != swr_init(swrContext)) {
        Logcat::e("HWVC", "HwResampler init failed");
        swr_free(&swrContext);
        swrContext = nullptr;
    }
}

HwResampler::~HwResampler() {

}

bool HwResampler::convert(HwBuffer *dest, HwBuffer *src) {
    if (!swrContext) {
        return false;
    }
    return true;
}