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

const int MEDIA_TYPE_UNKNOWN = -1;
const int MEDIA_TYPE_EOS = 0;
const int MEDIA_TYPE_VIDEO = 1;
const int MEDIA_TYPE_AUDIO = 2;

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
    int grab(AVFrame *avFrame);

    int width();

    int height();

private:
    string path;
    AVFormatContext *pFormatCtx = nullptr;
    AVCodecContext *codecContext = nullptr;
    int audioTrack = -1, videoTrack = -1, currentTrack = -1;
    AVPacket *avPacket = nullptr;

    int getMediaType(int track);
};

#ifdef __cplusplus
}
#endif

#endif //HARDWAREVIDEOCODEC_DECODER_H
