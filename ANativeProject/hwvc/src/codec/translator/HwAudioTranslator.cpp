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
                                    outFormat.getChannels(),
                                    HwAbsMediaFrame::convertAudioFrameFormat(outFormat.getFormat()),
                                    outFormat.getSampleRate(),
                                    inFormat.getChannels(),
                                    HwAbsMediaFrame::convertAudioFrameFormat(inFormat.getFormat()),
                                    inFormat.getSampleRate(),
                                    0, nullptr);
    if (!swrContext || 0 != swr_init(swrContext)) {
        Logcat::e("HWVC", "HwAudioTranslator init failed");
        swr_free(&swrContext);
        swrContext = nullptr;
    }
    Logcat::i("HWVC", "HwAudioTranslator(%lld, %d, %d <- %lld, %d, %d)",
              outFormat.getChannels(),
              HwAbsMediaFrame::convertAudioFrameFormat(outFormat.getFormat()),
              outFormat.getSampleRate(),
              inFormat.getChannels(),
              HwAbsMediaFrame::convertAudioFrameFormat(inFormat.getFormat()),
              inFormat.getSampleRate());

}

HwAudioTranslator::~HwAudioTranslator() {
    if (swrContext) {
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
    if (!outFrame || outFrame->nb_samples != (*src)->nb_samples ||
        outFrame->sample_rate != outFormat.getSampleRate() ||
        outFrame->channels != outFormat.getChannels()) {
        if (outFrame) {
            av_frame_free(&outFrame);
        }
        int nbSample = outFormat.getSampleRate() * (*src)->nb_samples / inFormat.getSampleRate();
        int byteCount = nbSample * HwAbsMediaFrame::getBytesPerSample(outFormat.getFormat()) *
                        outFormat.getChannels();
        outFrame = av_frame_alloc();
        outFrame->nb_samples = nbSample;
        outFrame->format = HwAbsMediaFrame::convertAudioFrameFormat(inFormat.getFormat());
        outFrame->channels = outFormat.getChannels();
        outFrame->channel_layout = av_get_default_channel_layout(outFormat.getChannels());
        outFrame->sample_rate = outFormat.getSampleRate();
        int ret = avcodec_fill_audio_frame(outFrame, inFormat.getChannels(),
                                           HwAbsMediaFrame::convertAudioFrameFormat(
                                                   inFormat.getFormat()),
                                           (const uint8_t *) av_malloc(byteCount), byteCount, 0);
        if (ret < 0) {
            return false;
        }
    }
    int ret = swr_convert_frame(swrContext, outFrame, src[0]);
    dest[0] = outFrame;
    return true;
}