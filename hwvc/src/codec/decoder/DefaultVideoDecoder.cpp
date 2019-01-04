/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/DefaultVideoDecoder.h"
#include "log.h"

#ifdef __cplusplus
extern "C" {
#endif
#include "x264.h"
#include "x264_config.h"

DefaultVideoDecoder::DefaultVideoDecoder() {

}

DefaultVideoDecoder::~DefaultVideoDecoder() {
    if (avPacket) {
        av_packet_unref(avPacket);
        avPacket = nullptr;
    }
    if (codecContext) {
        avcodec_close(codecContext);
        codecContext = nullptr;
    }
    if (pFormatCtx) {
        avformat_free_context(pFormatCtx);
        pFormatCtx = nullptr;
    }
}

bool DefaultVideoDecoder::prepare(string path) {
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
    LOGI("DefaultVideoDecoder(%s) %d x %d", path.c_str(), avCodecParameters->width, avCodecParameters->height);
    AVCodec *codec = avcodec_find_decoder(avCodecParameters->codec_id);
    if (NULL == codec) {
        LOGE("Couldn't find codec.");
        return false;
    }
    //打开编码器
    codecContext = avcodec_alloc_context3(codec);
    avcodec_parameters_to_context(codecContext, avCodecParameters);
    if (avcodec_open2(codecContext, codec, NULL) < 0) {
        LOGE("Couldn't open codec.");
        return false;
    }
    //准备资源
    avPacket = av_packet_alloc();
    return true;
}

int DefaultVideoDecoder::grab(AVFrame *avFrame) {
    if (currentTrack >= 0 && 0 == avcodec_receive_frame(codecContext, avFrame)) {
//        LOGI("avcodec_receive_frame");
        return getMediaType(currentTrack);
    }
    if (avPacket) {
        av_packet_unref(avPacket);
    }
    if (av_read_frame(pFormatCtx, avPacket) >= 0) {
//        LOGI("av_read_frame");
        currentTrack = avPacket->stream_index;
        //解码
        int ret = 0;
        if (videoTrack == currentTrack) {
            if ((ret = avcodec_send_packet(codecContext, avPacket)) == 0) {
                // 一个avPacket可能包含多帧数据，所以需要使用while循环一直读取
                return grab(avFrame);
            }
            switch (ret) {
                case AVERROR(EAGAIN): {
                    LOGI("you must read output with avcodec_receive_frame");
                    break;
                }
                case AVERROR(EINVAL): {
                    LOGI("codec not opened, it is an encoder, or requires flush");
                    break;
                }
                case AVERROR(ENOMEM): {
                    LOGI("failed to add packet to internal queue");
                    break;
                }
                case AVERROR_EOF: {
                    LOGI("eof");
                    break;
                }
                default:
                    LOGI("avcodec_send_packet ret=%d", ret);
            }
        } else if (audioTrack == currentTrack) {
            return MEDIA_TYPE_AUDIO;
        }
    }
    return MEDIA_TYPE_EOF;
}

int DefaultVideoDecoder::width() {
    if (!pFormatCtx) return 0;
    return pFormatCtx->streams[videoTrack]->codecpar->width;
}

int DefaultVideoDecoder::height() {
    if (!pFormatCtx) return 0;
    return pFormatCtx->streams[videoTrack]->codecpar->height;
}

int DefaultVideoDecoder::getMediaType(int track) {
    if (videoTrack == track) {
        return MEDIA_TYPE_VIDEO;
    }
    if (audioTrack == track) {
        return MEDIA_TYPE_AUDIO;
    }
    return MEDIA_TYPE_UNKNOWN;
}

#ifdef __cplusplus
}
#endif