//
// Created by nonolive66 on 2018/7/25.
//
#include <log.h>
#include <string.h>
#include "librtmp/rtmp.h"
#include "SpecificData.h"

#ifndef HARDWAREVIDEOCODEC_RTMP_H
#define HARDWAREVIDEOCODEC_RTMP_H

#define ERROR_DISCONNECT  -100

class RtmpClient {
public:
    /**
     * 连接rtmp服务
     */
    int connect(char *url, int timeOut);

    /**
     * 新建流连接
     */
    int connectStream(int w, int h);

    /**
     * 删除流连接
     */
    void deleteStream();

    /**
     * 发送sps、pps 帧
     */
    int sendVideoSpecificData(char *sps, int spsLen, char *pps, int ppsLen);

    /**
     * 发送视频帧
     */
    int sendVideo(char *data, int len, long timestamp);

    /**
     * 发送音频关键帧
     */
    int sendAudioSpecificData(char *data, int len);

    /**
     * 发送音频数据
     */
    int sendAudio(char *data, int len, long timestamp);

    /**
     * 释放资源
     */
    void stop();

    ~RtmpClient();

private:
    SpecificData *sps = NULL, *pps = NULL, *spec = NULL;
    long videoCount = 0, audioCount = 0;
    long retryTime[3] = {3000, 9000, 27000};
    int curRetryCount = 0;
    int width;
    int height;
    int timeOut;
    char *url;
    long startTime;
    RTMP *rtmp;

    void saveVideoSpecificData(char *sps, int spsLen, char *pps, int ppsLen);

    void saveAudioSpecificData(char *spec, int len);

    int sendVideoSpecificData(SpecificData *sps, SpecificData *pps);

    int sendAudioSpecificData(SpecificData *spec);
};

#endif //HARDWAREVIDEOCODEC_RTMP_H
