/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_DEFAULTVIDEODECODER_H
#define HARDWAREVIDEOCODEC_DEFAULTVIDEODECODER_H

#include "AbsVideoDecoder.h"
#include "AbsAudioDecoder.h"

const int MEDIA_TYPE_UNKNOWN = -1;
const int MEDIA_TYPE_EOF = 0;
const int MEDIA_TYPE_VIDEO = 1;
const int MEDIA_TYPE_AUDIO = 2;

#ifdef __cplusplus
extern "C" {
#endif

#include "ff/libavcodec/avcodec.h"
#include "ff/libavformat/avformat.h"
#include "ff/libavutil/avutil.h"
#include "ff/libswresample/swresample.h"


class DefaultVideoDecoder : public AbsVideoDecoder, public AbsAudioDecoder {
public:
    DefaultVideoDecoder();

    virtual ~DefaultVideoDecoder();

    virtual bool prepare(string path) override;

    virtual int width() override;

    virtual int height() override;

    virtual int getChannels() override;

    virtual int getSampleHz() override;

    virtual int getSampleFormat() override;

    virtual int getPerSampleSize() override;

    virtual void seek(int64_t us) override;

    /**
     * @return 1: video, 2: audio, 0: failed
     */
    virtual int grab(AVFrame *avFrame);

    virtual int64_t getVideoDuration() override;

    virtual int64_t getAudioDuration() override;

private:
    string path;
    AVFormatContext *pFormatCtx = nullptr;
    AVCodecContext *vCodecContext = nullptr;
    AVCodecContext *aCodecContext = nullptr;
    SwrContext *swrContext = nullptr;
    int audioTrack = -1, videoTrack = -1, currentTrack = -1;
    AVPacket *avPacket = nullptr;
    AVFrame *resampleFrame = nullptr;
    AVSampleFormat outputSampleFormat = AV_SAMPLE_FMT_S16;
    AVRational outputRational = AVRational{1, 1000000};
    int64_t videoDurationUs = -1;
    int64_t audioDurationUs = -1;

    int initSwr();

    int getMediaType(int track);

    bool openTrack(int track, AVCodecContext **context);

    void printCodecInfo();

    void resample(AVFrame *avFrame);

    AVSampleFormat getBestSampleFormat(AVSampleFormat in);

    void matchPts(AVFrame *frame, int track);
};

#ifdef __cplusplus
}
#endif

#endif //HARDWAREVIDEOCODEC_DEFAULTVIDEODECODER_H
