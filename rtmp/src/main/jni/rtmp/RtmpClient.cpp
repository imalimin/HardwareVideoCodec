/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "RtmpClient.h"
#include "librtmp/log.h"

#define WHAT_CONNECT 10
#define WHAT_CONNECT_STREAM 11
#define WHAT_SEND_VSD 12
#define WHAT_SEND_V 13
#define WHAT_SEND_ASD 14
#define WHAT_SEND_A 15

#define RTMP_HEAD_SIZE (sizeof(RTMPPacket)+RTMP_MAX_HEADER_SIZE)
#define NAL_SLICE  1
#define NAL_SLICE_DPA  2
#define NAL_SLICE_DPB  3
#define NAL_SLICE_DPC  4
#define NAL_SLICE_IDR  5
#define NAL_SEI  6
#define NAL_SPS  7
#define NAL_PPS  8
#define NAL_AUD  9
#define NAL_FILLER  12

#define STREAM_CHANNEL_METADATA  0x03
#define STREAM_CHANNEL_VIDEO     0x04
#define STREAM_CHANNEL_AUDIO     0x05

static void handleMessage(Message *msg) {
    switch (msg->what) {
        case WHAT_CONNECT: {
            Connection *con = reinterpret_cast<Connection *>(msg->obj);
            con->client->_connect(con->url, con->timeOut);
            msg->destory<Connection>();
            break;
        }
        case WHAT_CONNECT_STREAM: {
            Size *size = reinterpret_cast<Size *>(msg->obj);
            size->client->_connectStream(size->width, size->height);
            msg->destory<Size>();
            break;
        }
        case WHAT_SEND_VSD: {
            ClientWrapper *wrapper = reinterpret_cast<ClientWrapper *>(msg->obj);
            wrapper->client->_sendVideoSpecificData();
            msg->destory<ClientWrapper>();
            break;
        }
        case WHAT_SEND_V: {
            Packet *pkt = reinterpret_cast<Packet *>(msg->obj);
            pkt->client->_sendVideo(pkt->data, pkt->size, pkt->timestamp);
            msg->destory<Packet>();
            break;
        }
        case WHAT_SEND_ASD: {
            ClientWrapper *wrapper = reinterpret_cast<ClientWrapper *>(msg->obj);
            wrapper->client->_sendAudioSpecificData();
            msg->destory<ClientWrapper>();
            break;
        }
        case WHAT_SEND_A: {
            Packet *pkt = reinterpret_cast<Packet *>(msg->obj);
            pkt->client->_sendAudio(pkt->data, pkt->size, pkt->timestamp);
            msg->destory<Packet>();
            break;
        }
        default:
            break;
    }
}

static Connection *wrapConnection(RtmpClient *client, char *url, int timeOutMs) {
    Connection *con = new Connection(client);
    con->timeOut = timeOutMs;
    int len = strlen(url);
    con->url = static_cast<char *>(malloc(sizeof(char) * len));
    strcpy(con->url, url);
    return con;
}

static Size *wrapSize(RtmpClient *client, int width, int height) {
    Size *size = new Size(client);
    size->width = width;
    size->height = height;
    return size;
}

static Packet *wrapPacket(RtmpClient *client, const char *data, int size, long timestamp) {
    Packet *pkt = new Packet(client);
    pkt->data = static_cast<char *>(malloc(sizeof(char) * size));
    memcpy(pkt->data, data, size);
    pkt->size = size;
    pkt->timestamp = timestamp;
    return pkt;
}

template<class T>
static int arraySizeof(T &array) {
    if (NULL == array) return 0;
    return sizeof(array) / sizeof(array[0]);
}

static bool isIDR(char *data) {
    if (NULL == data) return false;
    int offset = 0;
    if (data[2] == 0x00) {
        offset = 4;
    } else if (data[2] == 0x01) {
        offset = 3;
    }
    return (data[offset] & 0x1f) == NAL_SLICE_IDR;
}

RtmpClient::RtmpClient(int cacheSize) {
    this->cacheSize = cacheSize;
    pipeline = new HandlerThread();
    LOGI("RTMP: init cache size: %d", this->cacheSize);
}

int RtmpClient::connect(char *url, int timeOutMs) {
    Connection *con = wrapConnection(this, url, timeOutMs);
    pipeline->sendMessage(obtainMessage(WHAT_CONNECT, con, handleMessage));
    return 1;
}

int RtmpClient::connectStream(int w, int h) {
    Size *size = wrapSize(this, w, h);
    pipeline->sendMessage(obtainMessage(WHAT_CONNECT_STREAM, size, handleMessage));
    return 1;
}

void RtmpClient::deleteStream() {
    RTMP_DeleteStream(rtmp);
}

int
RtmpClient::sendVideoSpecificData(const char *sps, int spsLen, const char *pps, int ppsLen) {
    saveVideoSpecificData(sps, spsLen, pps, ppsLen);
    pipeline->sendMessage(obtainMessage(WHAT_SEND_VSD, new ClientWrapper(this), handleMessage));
    return 0;
}

int RtmpClient::sendVideo(const char *data, int len, long timestamp) {
    if (dropMessage()) {
//        LOGI("RTMP: drop video, cache=%d", pipeline->size());
    }
    Packet *pkt = wrapPacket(this, data, len, timestamp);
    pipeline->sendMessage(obtainMessage(WHAT_SEND_V, pkt, handleMessage));
    return 0;
}

int RtmpClient::sendAudioSpecificData(const char *data, int len) {
    saveAudioSpecificData(data, len);
    pipeline->sendMessage(obtainMessage(WHAT_SEND_ASD, new ClientWrapper(this), handleMessage));
    return 0;
}

int RtmpClient::sendAudio(const char *data, int len, long timestamp) {
    if (dropMessage()) {
//        LOGI("RTMP: drop audio, cache=%d", pipeline->size());
    }
    Packet *pkt = wrapPacket(this, data, len, timestamp);
    pipeline->sendMessage(obtainMessage(WHAT_SEND_A, pkt, handleMessage));
    return 0;
}

void RtmpClient::stop() {
    if (NULL != rtmp) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }
    if (NULL != sps) {
        delete sps;
        sps = NULL;
    }
    if (NULL != pps) {
        delete pps;
        pps = NULL;
    }
    if (NULL != spec) {
        delete spec;
        spec = NULL;
    }
    if (NULL != pipeline) {
        pipeline->quit();
        pipeline = NULL;
    }
    LOGI("RTMP: stop");
}

RtmpClient::~RtmpClient() {
}

int RtmpClient::_connect(char *url, int timeOutMs) {
    LOGI("RTMP: connect: %s", url);
    this->url = url;
    this->timeOutMs = timeOutMs;

    RTMP_LogSetLevel(RTMP_LOGALL);
    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
    rtmp->Link.timeout = timeOutMs / 1000;
    RTMP_SetupURL(rtmp, url);
    RTMP_EnableWrite(rtmp);
    int ret = 1, retry = -1, count = arraySizeof(retryTime);
    while (retry < count) {
        LOGI("RTMP: try connect(%d)", retry);
        if ((ret = RTMP_Connect(rtmp, NULL)) <= 0) {
            LOGE("RTMP: connect failed! ");
            ++retry;
            pipeline->sleep(retryTime[retry]);
        } else {
            LOGI("RTMP: connect success! ");
            break;
        }
    }
    return ret;
}

int RtmpClient::_connectStream(int w, int h) {
    if (NULL == rtmp || !RTMP_IsConnected(rtmp)) {
        if (connect(this->url, this->timeOutMs) < 0) {
            LOGE("RTMP: You must connected before connect stream!");
            return ERROR_DISCONNECT;
        }
    }
    this->width = w;
    this->height = h;
    this->videoCount = 0;
    this->audioCount = 0;
    int ret = 1, retry = -1, count = arraySizeof(retryTime);
    while (retry < count) {
        LOGI("RTMP: try connectStream(%d), %dx%d", retry, this->width, this->height);
        if ((ret = RTMP_ReconnectStream(rtmp, 0)) <= 0) {
            LOGE("RTMP: connectStream failed: %d", ret);
            ++retry;
            pipeline->sleep(retryTime[retry]);
        } else {
            LOGI("RTMP: connectStream success", ret);
            break;
        }
    }
    if (this->sps && this->pps) {
        sendVideoSpecificData(this->sps, this->pps);
    }
    if (this->spec) {
        sendAudioSpecificData(this->spec);
    }
    return ret;
}

int RtmpClient::_sendVideoSpecificData() {
    return sendVideoSpecificData(this->sps, this->pps);
}

int RtmpClient::sendVideoSpecificData(SpecificData *sps, SpecificData *pps) {
    if (NULL == rtmp || !RTMP_IsConnected(rtmp)) {
        return ERROR_DISCONNECT;
    }
    if (NULL != sps && sps->alreadySent()) {
        return 1;
    }
    LOGI("RTMP: sendVideoSpecificData, IsConnected(%d)", RTMP_IsConnected(rtmp));
    RTMPPacket *packet = makeVideoSpecificData(sps, pps);
    int ret = ERROR_DISCONNECT;
    if (RTMP_IsConnected(rtmp)) {
        ret = RTMP_SendPacket(rtmp, packet, TRUE);
        sps->setSent(true);
        pps->setSent(true);
    }
    free(packet);
    return ret;
}

int RtmpClient::_sendVideo(char *data, int len, long timestamp) {
    if (NULL == rtmp || NULL == sps || !sps->alreadySent()) return -1;
    if (len < 1) return -2;
    RTMPPacket *packet = makeVideoPacket(data, len, timestamp);
    int ret = ERROR_DISCONNECT;
    if (RTMP_IsConnected(rtmp)) {
        ret = RTMP_SendPacket(rtmp, packet, TRUE);
        if (0 == videoCount % 150)
            LOGI("RTMP: send video packet(%ld): %d, cache=%d", videoCount, len, pipeline->size());
        ++videoCount;
    }
    free(packet);
    return ret;
}

int RtmpClient::_sendAudioSpecificData() {
    return sendAudioSpecificData(this->spec);
}

int RtmpClient::sendAudioSpecificData(SpecificData *spec) {
    if (NULL == rtmp || !RTMP_IsConnected(rtmp)) {
        return ERROR_DISCONNECT;
    }
    if (NULL != spec && spec->alreadySent()) {
        return 1;
    }
    LOGI("RTMP: sendAudioSpecificData, IsConnected(%d)", RTMP_IsConnected(rtmp));
    RTMPPacket *packet = makeAudioSpecificData(spec);
    int ret = ERROR_DISCONNECT;
    if (RTMP_IsConnected(rtmp)) {
        ret = RTMP_SendPacket(rtmp, packet, TRUE);
        spec->setSent(true);
    }
    free(packet);
    return ret;
}

int RtmpClient::_sendAudio(char *data, int len, long timestamp) {
    if (NULL == rtmp || NULL == spec || !spec->alreadySent()) return -1;
    if (len < 1) return -2;
    RTMPPacket *packet = makeAudioPacket(data, len, timestamp);
    int ret = ERROR_DISCONNECT;
    if (RTMP_IsConnected(rtmp)) {
        ret = RTMP_SendPacket(rtmp, packet, TRUE);
        if (0 == audioCount % 150)
            LOGI("RTMP: send audio packet(%ld): %d, cache=%d", audioCount, len, pipeline->size());
        ++audioCount;
    }
    free(packet);
    return ret;
}

static bool filter_count = 0;

static short idrFilter(Message *msg) {
    if (WHAT_SEND_V != msg->what && WHAT_SEND_A != msg->what) return 0;
    Packet *pkt = (Packet *) msg->obj;
    if (isIDR(pkt->data))
        ++filter_count;
    if (FILTER_REMOVE == filter_count)
        msg->destory<Packet>();
    return filter_count;
}

static short frameFilter(Message *msg) {
    if (WHAT_SEND_V != msg->what && WHAT_SEND_A != msg->what) return FILTER_DO_NOTHING;
    if (WHAT_SEND_V == msg->what)
        ++filter_count;
    if (FILTER_REMOVE == filter_count)
        msg->destory<Packet>();
    return filter_count;
}

bool RtmpClient::dropMessage() {
    int size = pipeline->size();
    if (size < this->cacheSize) return false;
    filter_count = 0;
    pipeline->removeAllMessage(idrFilter);
    /**
     * If it drop too little, drop it again.
     */
    if (size - pipeline->size() <= 10) {
        filter_count = 0;
        pipeline->removeAllMessage(idrFilter);
        LOGI("RTMP: drop again");
    }
    /**
     * If no frame is dropped, the data is discarded between the two video frames.
     */
    if (0 == size - pipeline->size()) {
        filter_count = 0;
        pipeline->removeAllMessage(frameFilter);
        LOGI("RTMP: drop video frames");
    }
    filter_count = 0;
    if (0 == pipeline->size()) {
        LOGE("RTMP: cacheSize too small");
    }
    LOGI("RTMP: drop %d(%d - %d) message", size - pipeline->size(), size, pipeline->size());
    return true;
}

void RtmpClient::setCacheSize(int size) {
    this->cacheSize = size;
}

RTMPPacket *RtmpClient::makeVideoPacket(char *data, int len, long timestamp) {
    /*去掉帧界定符*/
    if (data[2] == 0x00) {/*00 00 00 01*/
        data += 4;
        len -= 4;
    } else if (data[2] == 0x01) {
        data += 3;
        len -= 3;
    }

    int type = data[0] & 0x1f;

    RTMPPacket *packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + len + 9);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    packet->m_nBodySize = static_cast<uint32_t>(len + 9);

    char *body = packet->m_body;
    memset(body, 0, static_cast<size_t>(len + 9));

    /*key frame*/
    body[0] = 0x27;
    if (type == NAL_SLICE_IDR) {
        body[0] = 0x17;
    }

    body[1] = 0x01;/*nal unit*/
    body[2] = 0x00;
    body[3] = 0x00;
    body[4] = 0x00;

    body[5] = static_cast<char>((len >> 24) & 0xff);
    body[6] = static_cast<char>((len >> 16) & 0xff);
    body[7] = static_cast<char>((len >> 8) & 0xff);
    body[8] = static_cast<char>((len) & 0xff);

    /*copy data*/
    memcpy(&body[9], data, static_cast<size_t>(len));

    packet->m_hasAbsTimestamp = 0;
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nInfoField2 = rtmp->m_stream_id;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nTimeStamp = static_cast<uint32_t>(timestamp);
    return packet;
}

RTMPPacket *RtmpClient::makeAudioPacket(char *data, int len, long timestamp) {
    RTMPPacket *packet;
    char *body;
    packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + len + 2);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    body = (char *) packet->m_body;

    /*AF 00 +AAC Raw data*/
    body[0] = static_cast<char>(0xAF);
    body[1] = 0x01;
    memcpy(&body[2], data, static_cast<size_t>(len));

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = static_cast<uint32_t>(len + 2);
    packet->m_nChannel = STREAM_CHANNEL_AUDIO;
    packet->m_nTimeStamp = static_cast<uint32_t>(timestamp);
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;
    return packet;
}

RTMPPacket *RtmpClient::makeVideoSpecificData(SpecificData *sps, SpecificData *pps) {
    RTMPPacket *packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + 1024);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    char *body = packet->m_body;

    int i = 0;
    body[i++] = 0x17; //1:keyframe 7:AVC
    body[i++] = 0x00; // AVC sequence header

    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00; //fill in 0

    /*AVCDecoderConfigurationRecord*/
    body[i++] = 0x01;
    body[i++] = sps->get()[1]; //AVCProfileIndecation
    body[i++] = sps->get()[2]; //profile_compatibilty
    body[i++] = sps->get()[3]; //AVCLevelIndication
    body[i++] = 0xff;//lengthSizeMinusOne

    /*SPS*/
    body[i++] = 0xe1;
    body[i++] = (sps->size() >> 8) & 0xff;
    body[i++] = sps->size() & 0xff;
    /*sps data*/
    memcpy(&body[i], sps->get(), sps->size());

    i += sps->size();

    /*PPS*/
    body[i++] = 0x01;
    /*sps data length*/
    body[i++] = static_cast<char>((pps->size() >> 8) & 0xff);
    body[i++] = static_cast<char>(pps->size() & 0xff);
    memcpy(&body[i], pps->get(), pps->size());
    i += pps->size();

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = static_cast<uint32_t>(i);
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    packet->m_nInfoField2 = rtmp->m_stream_id;
    return packet;
}

RTMPPacket *RtmpClient::makeAudioSpecificData(SpecificData *spec) {
    RTMPPacket *packet;
    char *body;
    packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + spec->size() + 2);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    body = packet->m_body;

    /*AF 00 +AAC RAW data*/
    body[0] = 0xAF;
    body[1] = 0x00;
    memcpy(&body[2], spec->get(), spec->size());/*data 是AAC sequeuece header数据*/

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = spec->size() + 2;
    packet->m_nChannel = STREAM_CHANNEL_AUDIO;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;
    return packet;
}

void RtmpClient::saveVideoSpecificData(const char *sps, int spsLen, const char *pps, int ppsLen) {
    if (NULL != this->sps) {
        delete this->sps;
    }
    if (NULL != this->pps) {
        delete this->pps;
    }
    this->sps = new SpecificData(sps, spsLen);
    this->pps = new SpecificData(pps, ppsLen);
    LOGI("RTMP: saveVideoSpecificData");
}

void RtmpClient::saveAudioSpecificData(const char *spec, int len) {
    if (NULL != this->spec) {
        delete this->spec;
    }
    this->spec = new SpecificData(spec, len);
    LOGI("RTMP: saveAudioSpecificData");
}
