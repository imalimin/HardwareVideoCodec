/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_DEFAULTVIDEODECODER_H
#define HARDWAREVIDEOCODEC_DEFAULTVIDEODECODER_H

#include "AbsVideoDecoder.h"

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


class DefaultVideoDecoder : public AbsVideoDecoder {
public:
    DefaultVideoDecoder();

    virtual ~DefaultVideoDecoder();

    bool prepare(string path) override;

    int width() override;

    int height() override;

    /**
     * @return 1: video, 2: audio, 0: failed
     */
    int grab(AVFrame *avFrame);

private:
    string path;
    AVFormatContext *pFormatCtx = nullptr;
    AVCodecContext *vCodecContext = nullptr;
    AVCodecContext *aCodecContext = nullptr;
    int audioTrack = -1, videoTrack = -1, currentTrack = -1;
    AVPacket *avPacket = nullptr;

    int getMediaType(int track);

    bool openTrack(int track, AVCodecContext **context);

    void printCodecInfo();
};

#ifdef __cplusplus
}
#endif

#endif //HARDWAREVIDEOCODEC_DEFAULTVIDEODECODER_H
