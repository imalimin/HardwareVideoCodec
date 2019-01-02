/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_DECODER_H
#define HARDWAREVIDEOCODEC_DECODER_H

#include "Object.h"
#include <string>

#ifdef __cplusplus
extern "C" {
#endif
#include "ff/libavcodec/avcodec.h"
#include "ff/libavformat/avformat.h"
#include "ff/libavutil/avutil.h"

using namespace std;

class Decoder : public Object {
public:
    Decoder();

    virtual ~Decoder();

    bool prepare(string path);

    /**
     * @return 1: video, 2: audio, 0: failed
     */
    int grab();

    int width();

    int height();

private:
    string path;
    AVFormatContext *pFormatCtx = nullptr;
    AVCodecContext *codecContext = nullptr;
    int audioTrack = -1, videoTrack = -1;
    AVPacket *avPacket = nullptr;
    AVFrame *avFrame = nullptr;
};

#ifdef __cplusplus
}
#endif

#endif //HARDWAREVIDEOCODEC_DECODER_H
