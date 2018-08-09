//
// Created by nonolive66 on 2018/7/25.
//
#include <log.h>
#include <string.h>
#include "librtmp/rtmp.h"
#include "SpecificData.h"
#include "HandlerThread.h"
#include "Object.h"
#include "Lock.h"

#define ERROR_DISCONNECT  -100

#ifndef HARDWAREVIDEOCODEC_RTMP_H
#define HARDWAREVIDEOCODEC_RTMP_H

class RtmpClient {
public:
    RtmpClient(int cacheSize);

    /**
     * 连接rtmp服务
     */
    int connect(char *url, int timeOutMs);

    int _connect(char *url, int timeOutMs);

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
    Lock *mutex;
    HandlerThread *pipeline = NULL;
    SpecificData *sps = NULL, *pps = NULL, *spec = NULL;
    long videoCount = 0, audioCount = 0;
    long retryTime[3] = {3000, 9000, 27000};
    int width;
    int height;
    int timeOutMs;
    char *url;
    RTMP *rtmp = NULL;

    void saveVideoSpecificData(const char *sps, int spsLen, const char *pps, int ppsLen);

    void saveAudioSpecificData(const char *spec, int len);

    int sendVideoSpecificData(SpecificData *sps, SpecificData *pps);

    int sendAudioSpecificData(SpecificData *spec);

    /*
     * Discard all data between two IDRs, including audio.
     * If cacheSize too small to cache two IDRs, an error may occur.
     */
    bool dropMessage();

    RTMPPacket *makeVideoPacket(char *data, int len, long timestamp);

    RTMPPacket *makeAudioPacket(char *data, int len, long timestamp);

    RTMPPacket *makeVideoSpecificData(SpecificData *sps, SpecificData *pps);

    RTMPPacket *makeAudioSpecificData(SpecificData *spec);
};

class ClientWrapper : public Object {
public:
    RtmpClient *client;

    ClientWrapper(RtmpClient *client) {
        this->client = client;
    }

    virtual ~ClientWrapper() {

    }
};

class Connection : public ClientWrapper {

public:
    Connection(RtmpClient *client) : ClientWrapper(client) {}

    char *url;
    int timeOut;

    virtual ~Connection() {

    }
};

class Size : public ClientWrapper {

public:
    Size(RtmpClient *client) : ClientWrapper(client) {}

    int width, height;

    virtual ~Size() {

    }
};

class Packet : public ClientWrapper {

public:
    Packet(RtmpClient *client) : ClientWrapper(client) {}

    char *data;
    int size;
    long timestamp;

    virtual ~Packet() {
//        LOGE("RTMP: release Packet");
        if (NULL != data) {
            free(data);
            data = NULL;
        }
    }
};

#endif //HARDWAREVIDEOCODEC_RTMP_H
