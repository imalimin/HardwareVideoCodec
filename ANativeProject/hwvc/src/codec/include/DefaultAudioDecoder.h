/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_DEFAULTAUDIODECODER_H
#define HARDWAREVIDEOCODEC_DEFAULTAUDIODECODER_H

#include "AbsAudioDecoder.h"
#include "HwAbsMediaFrame.h"
#include "HwFrameAllocator.h"
#include "HwAudioTranslator.h"

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
    virtual int grab(HwAbsMediaFrame **frame);

    int getChannels();

    int getSampleHz();

    int getSampleFormat();

    int getSamplesPerBuffer();

    int64_t getAudioDuration();

private:
    bool openTrack(int track, AVCodecContext **context);

    AVSampleFormat getBestSampleFormat(AVSampleFormat in);

    HwAbsMediaFrame *resample(AVFrame *avFrame);

    void matchPts(AVFrame *frame, int track);

    void printCodecInfo();

private:
    bool enableDebug = false;
    string path;
    HwFrameAllocator *hwFrameAllocator = nullptr;
    AVFormatContext *pFormatCtx = nullptr;
    AVCodecContext *aCodecContext = nullptr;
    HwAudioTranslator *translator = nullptr;
    int audioTrack = -1;
    AVPacket *avPacket = nullptr;
    AVFrame *audioFrame = nullptr;
    HwAbsMediaFrame *outHwFrame = nullptr;
    AVSampleFormat outSampleFormat = AV_SAMPLE_FMT_S16;
    AVRational outputTimeBase = AVRational{1, 1000000};
    int64_t audioDurationUs = -1;
    bool eof = false;
    SimpleLock readPkgLock;
};


#endif //HARDWAREVIDEOCODEC_DEFAULTAUDIODECODER_H
