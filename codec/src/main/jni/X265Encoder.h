//
// Created by nonolive66 on 2018/6/13.
//
#include <log.h>
#include <malloc.h>
#include <string.h>
#include <sys/time.h>
#include <x265.h>
#include "libyuv.h"

#define X265_TYPE_HEADER          -0x0001  /* Headers SPS/PPS */
#define INVALID 0//未初始化
#define START 1//开始
#define STOP 1//停止
#ifndef HARDWAREVIDEOCODEC_X265_ENCODER_H
#define HARDWAREVIDEOCODEC_X265_ENCODER_H

class X265Encoder {
public:
    X265Encoder();

    ~X265Encoder();

    bool start();

    void stop();

    bool encode(char *src, char *dest, int *size, int *type);

    bool flush(char *dest, int *size, int *type);

    void setVideoSize(int width, int height);

    void setBitrate(int bitrate);

    void setFrameFormat(int format);

    void setFps(int fps);

private:
    bool fillSrc(char *argb);

    bool encodeHeader(char *dest, int *size, int *type);

    void reset();
};

#endif //HARDWAREVIDEOCODEC_X265_ENCODER_H