/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_DEFAULTAUDIODECODER_H
#define HARDWAREVIDEOCODEC_DEFAULTAUDIODECODER_H

#include "AbsAudioDecoder.h"
#include "HwAbsFrame.h"
#include "HwFrameAllocator.h"

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

class DefaultAudioDecoder : public AbsAudioDecoder {
public:
    DefaultAudioDecoder();

    virtual ~DefaultAudioDecoder();

    bool prepare(string path);

    void seek(int64_t us);

    /**
     * @return 1: video, 2: audio, 0: failed
     */
    virtual int grab(HwAbsFrame **frame);

    int getChannels();

    int getSampleHz();

    int getSampleFormat();

    int getPerSampleSize();

    int64_t getAudioDuration();

private:
    int initSwr();

    bool openTrack(int track, AVCodecContext **context);

    AVSampleFormat getBestSampleFormat(AVSampleFormat in);

    HwAbsFrame *resample(AVFrame *avFrame);

    void matchPts(AVFrame *frame, int track);

    void printCodecInfo();

private:
    string path;
    HwFrameAllocator *hwFrameAllocator = nullptr;
    AVFormatContext *pFormatCtx = nullptr;
    AVCodecContext *aCodecContext = nullptr;
    SwrContext *swrContext = nullptr;
    int audioTrack = -1;
    AVPacket *avPacket = nullptr;
    AVFrame *resampleFrame = nullptr;
    AVFrame *audioFrame = nullptr;
    HwAbsFrame *outputFrame;
    AVSampleFormat outputSampleFormat = AV_SAMPLE_FMT_S16;
    AVRational outputRational = AVRational{1, 1000000};
    int64_t audioDurationUs = -1;
    bool eof = false;
};


#endif //HARDWAREVIDEOCODEC_DEFAULTAUDIODECODER_H
