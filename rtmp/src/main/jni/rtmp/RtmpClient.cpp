/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "RtmpClient.h"
#include "librtmp/log.h"

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

int RtmpClient::connect(char *url, int w, int h, int timeOut) {
    this->url = url;
    this->width = w;
    this->height = h;
    this->timeOut = timeOut;

    RTMP_LogSetLevel(RTMP_LOGDEBUG);
    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
    rtmp->Link.timeout = timeOut;
    RTMP_SetupURL(rtmp, url);
    RTMP_EnableWrite(rtmp);

    if (RTMP_Connect(rtmp, NULL) <= 0) {
        stop();
        LOGE("RTMP_Connect failed!");
        return -1;
    }

    if (RTMP_ConnectStream(rtmp, 0) <= 0) {
        stop();
        LOGE("RTMP_ConnectStream failed!");
        return -1;
    }
    return 0;
}

int RtmpClient::sendSpsAndPps(char *sps, int spsLen, char *pps, int ppsLen, long timestamp) {
    int i;
    RTMPPacket *packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + 1024);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    char *body = packet->m_body;

    i = 0;
    body[i++] = 0x17; //1:keyframe 7:AVC
    body[i++] = 0x00; // AVC sequence header

    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00; //fill in 0

    /*AVCDecoderConfigurationRecord*/
    body[i++] = 0x01;
    body[i++] = sps[1]; //AVCProfileIndecation
    body[i++] = sps[2]; //profile_compatibilty
    body[i++] = sps[3]; //AVCLevelIndication
    body[i++] = 0xff;//lengthSizeMinusOne

    /*SPS*/
    body[i++] = 0xe1;
    body[i++] = (spsLen >> 8) & 0xff;
    body[i++] = spsLen & 0xff;
    /*sps data*/
    memcpy(&body[i], sps, spsLen);

    i += spsLen;

    /*PPS*/
    body[i++] = 0x01;
    /*sps data length*/
    body[i++] = (ppsLen >> 8) & 0xff;
    body[i++] = ppsLen & 0xff;
    memcpy(&body[i], pps, ppsLen);
    i += ppsLen;

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = i;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    /*发送*/
    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, TRUE);
    }
    free(packet);
    return 0;
}

int RtmpClient::sendVideoData(char *data, int len, long timestamp) {
    int type;

    /*去掉帧界定符*/
    if (data[2] == 0x00) {/*00 00 00 01*/
        data += 4;
        len -= 4;
    } else if (data[2] == 0x01) {
        data += 3;
        len - 3;
    }

    type = data[0] & 0x1f;

    RTMPPacket *packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + len + 9);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    packet->m_nBodySize = len + 9;


    /* send video packet*/
    char *body = packet->m_body;
    memset(body, 0, len + 9);

    /*key frame*/
    body[0] = 0x27;
    if (type == NAL_SLICE_IDR) {
        body[0] = 0x17; //关键帧
    }

    body[1] = 0x01;/*nal unit*/
    body[2] = 0x00;
    body[3] = 0x00;
    body[4] = 0x00;

    body[5] = (len >> 24) & 0xff;
    body[6] = (len >> 16) & 0xff;
    body[7] = (len >> 8) & 0xff;
    body[8] = (len) & 0xff;

    /*copy data*/
    memcpy(&body[9], data, len);

    packet->m_hasAbsTimestamp = 0;
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nInfoField2 = rtmp->m_stream_id;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nTimeStamp = timestamp;

    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, TRUE);
    }
    free(packet);

    return 0;
}

int RtmpClient::sendAacSpec(char *data, int len) {
    RTMPPacket *packet;
    char *body;
    packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + len + 2);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    body = packet->m_body;

    /*AF 00 +AAC RAW data*/
    body[0] = 0xAF;
    body[1] = 0x00;
    memcpy(&body[2], data, len);/*data 是AAC sequeuece header数据*/

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = len + 2;
    packet->m_nChannel = STREAM_CHANNEL_AUDIO;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, TRUE);
    }
    free(packet);

    return 0;
}

int RtmpClient::sendAacData(char *data, int len, long timestamp) {
    if (len > 0) {
        RTMPPacket *packet;
        char *body;
        packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + len + 2);
        memset(packet, 0, RTMP_HEAD_SIZE);
        packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
        body = (char *) packet->m_body;

        /*AF 00 +AAC Raw data*/
        body[0] = 0xAF;
        body[1] = 0x01;
        memcpy(&body[2], data, len);

        packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
        packet->m_nBodySize = len + 2;
        packet->m_nChannel = STREAM_CHANNEL_AUDIO;
        packet->m_nTimeStamp = timestamp;
        packet->m_hasAbsTimestamp = 0;
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
        packet->m_nInfoField2 = rtmp->m_stream_id;
        if (RTMP_IsConnected(rtmp)) {
            RTMP_SendPacket(rtmp, packet, TRUE);
        }

        LOGI("send packet body[0]=%x,body[1]=%x", body[0], body[1]);
        free(packet);
    }
    return 0;
}

void RtmpClient::stop() const {
    RTMP_Close(rtmp);
    RTMP_Free(rtmp);
}

RtmpClient::~RtmpClient() {
    stop();
}
