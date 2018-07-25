//
// Created by nonolive66 on 2018/7/25.
//
#include <log.h>
#include <string.h>
#include "librtmp/rtmp.h"

#ifndef HARDWAREVIDEOCODEC_RTMP_H
#define HARDWAREVIDEOCODEC_RTMP_H

class RtmpClient {
public:
    /**
     * 初始化
     */
    int connect(char *url, int w, int h, int timeOut);

    /**
     * 发送sps、pps 帧
     */
    int sendSpsAndPps(char *sps, int spsLen, char *pps, int ppsLen, long timestamp);

    /**
     * 发送视频帧
     */
    int sendVideoData(char *data, int len, long timestamp);

    /**
     * 发送音频关键帧
     */
    int sendAacSpec(char *data, int len);

    /**
     * 发送音频数据
     */
    int sendAacData(char *data, int len, long timestamp);

    /**
     * 释放资源
     */
    void stop() const;

    ~RtmpClient();

private:
    int width;
    int height;
    int timeOut;
    char *url;
    long startTime;
    RTMP *rtmp;
};

#endif //HARDWAREVIDEOCODEC_RTMP_H
