/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <log.h>
#include <malloc.h>
#include <string.h>
#include <sys/time.h>
#include "libyuv.h"
#include "x264.h"

#define X264_TYPE_HEADER          -0x0001  /* Headers SPS/PPS */
#define INVALID 0//未初始化
#define START 1//开始
#define STOP 1//停止
#define X264_CSP_RGBA 0x0100
#ifndef HARDWAREVIDEOCODEC_X264_ENCODER_H
#define HARDWAREVIDEOCODEC_X264_ENCODER_H

class X264Encoder {
public:
    X264Encoder(int fmt);

    ~X264Encoder();

    bool start();

    void stop();

    bool encode(char *src, char *dest, int *size, int *type);

    bool flush(char *dest, int *size, int *type);

    void setVideoSize(int width, int height);

    void setBitrate(int bitrate);

    void setFrameFormat(int format);

    void setFps(int fps);

    void setProfile(char *profile);

    void setLevel(int level);

private:

    bool encodeHeader(char *dest, int *size, int *type);

    void reset();
};

#endif //HARDWAREVIDEOCODEC_X264_ENCODER_H