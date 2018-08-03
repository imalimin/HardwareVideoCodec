//
// Created by nonolive66 on 2018/7/25.
//
#include <log.h>
#include <string.h>
#include "librtmp/rtmp.h"
#include "SpecificData.h"
#include "HandlerThread.h"

#define ERROR_DISCONNECT  -100

#ifndef HARDWAREVIDEOCODEC_RTMP_H
#define HARDWAREVIDEOCODEC_RTMP_H

class RtmpClient {
public:
    RtmpClient(int cacheSize);

    /**
     * 连接rtmp服务
     */
    int connect(char *url, int timeOut);

    int _connect(char *url, int timeOut);

    /**
     * 新建流连接
     */
    int connectStream(int w, int h);

    int _connectStream(int w, int h);

    /**
     * 删除流连接
     */
    void deleteStream();

    /**
     * 发送sps、pps 帧
     */
    int sendVideoSpecificData(const char *sps, int spsLen, const char *pps, int ppsLen);

    int _sendVideoSpecificData();

    /**
     * 发送视频帧
     */
    int sendVideo(const char *data, int len, long timestamp);

    int _sendVideo(char *data, int len, long timestamp);

    /**
     * 发送音频关键帧
     */
    int sendAudioSpecificData(const char *data, int len);

    int _sendAudioSpecificData();

    /**
     * 发送音频数据
     */
    int sendAudio(const char *data, int len, long timestamp);

    int _sendAudio(char *data, int len, long timestamp);

    /**
     * 释放资源
     */
    void stop();

    void setCacheSize(int size);

    ~RtmpClient();

private:
    int cacheSize;
    HandlerThread *pipeline = NULL;
    SpecificData *sps = NULL, *pps = NULL, *spec = NULL;
    long videoCount = 0, audioCount = 0;
    long retryTime[3] = {3000, 9000, 27000};
    int width;
    int height;
    int timeOut;
    char *url;
    long startTime;
    RTMP *rtmp;

    void saveVideoSpecificData(const char *sps, int spsLen, const char *pps, int ppsLen);

    void saveAudioSpecificData(const char *spec, int len);

    int sendVideoSpecificData(SpecificData *sps, SpecificData *pps);

    int sendAudioSpecificData(SpecificData *spec);

    bool dropMessage(int what);
};

class ClientWrapper {
public:
    RtmpClient *client;

};

class Connection : public ClientWrapper {
public:
    char *url;
    int timeOut;
};

class Size : public ClientWrapper {
public:
    int width, height;
};

class Packet : public ClientWrapper {
public:
    char *data;
    int size;
    long timestamp;

    ~Packet() {
        if (NULL != data) {
            free(data);
            data = NULL;
        }
    }
};

#endif //HARDWAREVIDEOCODEC_RTMP_H
