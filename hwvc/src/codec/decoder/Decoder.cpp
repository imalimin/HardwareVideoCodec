/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/Decoder.h"
#include "log.h"

#ifdef __cplusplus
extern "C" {
#endif
#include "x264.h"
#include "x264_config.h"

Decoder::Decoder() {

}

Decoder::~Decoder() {

}

bool Decoder::prepare(string path) {
    this->path = path;
    av_register_all();
    pFormatCtx = avformat_alloc_context();
    //打开输入视频文件
    if (avformat_open_input(&pFormatCtx, path.c_str(), NULL, NULL) != 0) {
        LOGE("Couldn't open input stream.");
        return false;
    }
    //获取视频文件信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("Couldn't find stream information.");
        return -1;
    }
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (-1 == videoTrack &&
            AVMediaType::AVMEDIA_TYPE_VIDEO == pFormatCtx->streams[i]->codecpar->codec_type) {
            videoTrack = i;
        }
        if (-1 == audioTrack &&
            AVMediaType::AVMEDIA_TYPE_AUDIO == pFormatCtx->streams[i]->codecpar->codec_type) {
            audioTrack = i;
        }
    }
    AVCodecParameters *avCodecParameters = pFormatCtx->streams[videoTrack]->codecpar;
    LOGI("Decoder(%s) %d x %d", path.c_str(), avCodecParameters->width, avCodecParameters->height);
    AVCodec *codec = avcodec_find_decoder(avCodecParameters->codec_id);
    if (NULL == codec) {
        LOGE("Couldn't find codec.");
        return false;
    }
    return true;
}

#ifdef __cplusplus
}
#endif