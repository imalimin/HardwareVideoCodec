//
// Created by mingyi.li on 2019/6/16.
//

#include "../include/HwAudioTranslator.h"
#include "Logcat.h"

HwAudioTranslator::HwAudioTranslator(HwSampleFormat outFormat, HwSampleFormat inFormat)
        : outFormat(outFormat),
          inFormat(inFormat),
          Object() {
    swrContext = swr_alloc_set_opts(swrContext,
                                    av_get_default_channel_layout(outFormat.getChannels()),
                                    HwAbsMediaFrame::convertAudioFrameFormat(outFormat.getFormat()),
                                    outFormat.getSampleRate(),
                                    av_get_default_channel_layout(inFormat.getChannels()),
                                    HwAbsMediaFrame::convertAudioFrameFormat(inFormat.getFormat()),
                                    inFormat.getSampleRate(),
                                    0, nullptr);
    if (!swrContext || 0 != swr_init(swrContext)) {
        Logcat::e("HWVC", "HwAudioTranslator init failed");
        if (swrContext) {
            swr_free(&swrContext);
            swrContext = nullptr;
        }
    }
    Logcat::i("HWVC", "HwAudioTranslator(%d, %d, %d <- %d, %d, %d)",
              outFormat.getChannels(),
              HwAbsMediaFrame::convertAudioFrameFormat(outFormat.getFormat()),
              outFormat.getSampleRate(),
              inFormat.getChannels(),
              HwAbsMediaFrame::convertAudioFrameFormat(inFormat.getFormat()),
              inFormat.getSampleRate());

}

HwAudioTranslator::~HwAudioTranslator() {
    if (swrContext) {
        swr_close(swrContext);
        swr_free(&swrContext);
        swrContext = nullptr;
    }
    if (outFrame) {
        av_frame_free(&outFrame);
        outFrame = nullptr;
    }
}

bool HwAudioTranslator::translate(AVFrame **dest, AVFrame **src) {
    if (!swrContext || !src || !(*src)) {
        return false;
    }
    int nbSample = outFormat.getSampleRate() * (*src)->nb_samples / inFormat.getSampleRate();
    if (!outFrame || outFrame->nb_samples != nbSample ||
        outFrame->sample_rate != outFormat.getSampleRate() ||
        outFrame->channels != outFormat.getChannels()) {
        if (outFrame) {
            av_frame_free(&outFrame);
        }
        outFrame = av_frame_alloc();
        outFrame->nb_samples = nbSample;
        outFrame->format = HwAbsMediaFrame::convertAudioFrameFormat(outFormat.getFormat());
//        outFrame->channels = outFormat.getChannels();
        outFrame->channel_layout = av_get_default_channel_layout(outFormat.getChannels());
        outFrame->sample_rate = outFormat.getSampleRate();
        outFrame->pts = 0;
        int ret = av_frame_get_buffer(outFrame, 0);
        if (0 != ret) {
            Logcat::e("HWVC", "HwAudioTranslator(%p) translate failed. avFrame alloc failed.",
                      this);
            return false;
        }
    }
    int ret = swr_convert_frame(swrContext, outFrame, src[0]);
    if (0 != ret) {
        Logcat::e("HWVC", "HwAudioTranslator(%p) translate failed.", this);
        return false;
    }
    outFrame->pts = (*src)->pts;
    dest[0] = outFrame;
    return true;
}