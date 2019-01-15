/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/AsynVideoDecoder.h"
#include "TimeUtils.h"

#ifdef __cplusplus
extern "C" {
#endif

AsynVideoDecoder::AsynVideoDecoder() : AbsDecoder(), AbsAudioDecoder(), AbsVideoDecoder() {
    decoder = new DefaultVideoDecoder();
    vRecycler = new RecyclerBlockQueue<AVFrame>(16, [] {
        return av_frame_alloc();
    });
}

AsynVideoDecoder::~AsynVideoDecoder() {
    lopping = false;
    if (vRecycler) {
        vRecycler->notify();
    }
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
    if (vRecycler) {
        vRecycler->clear();
        delete vRecycler;
        vRecycler = nullptr;
    }
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
}

bool AsynVideoDecoder::prepare(string path) {
    if (decoder) {
        if (!decoder->prepare(path)) {
            return false;
        }
    }
    if (!pipeline) {
        pipeline = new EventPipeline("AsynVideoDecoder");
    }
    lopping = true;
    loop();
    return true;
}

int AsynVideoDecoder::grab(Frame *frame) {
    AVFrame *f = vRecycler->take();
    if (AV_SAMPLE_FMT_S32 == f->format || AV_SAMPLE_FMT_FLT == f->format) {
        int size = 0;
        //对于音频，只有linesize[0]被使用，因为音频中，每一个声道的大小应该相等
        memcpy(frame->data + size, f->data[0], f->linesize[0]);
        size += f->linesize[0];
        frame->offset = 0;
        frame->size = size;
        LOGI("audio channels=%d, size=%d, nb_samples=%d, %d", f->channels, size, f->nb_samples,
             f->linesize[0]);
        av_frame_unref(f);
        vRecycler->recycle(f);
        return MEDIA_TYPE_AUDIO;
    } else {

    }
    if (AV_PIX_FMT_NV12 == f->format) {
        copyNV12(frame, f);
    } else {
        copyYV12(frame, f);
    }

    frame->width = f->width;
    frame->height = f->height;
    av_frame_unref(f);
    vRecycler->recycle(f);

    return MEDIA_TYPE_VIDEO;
}

void AsynVideoDecoder::copyYV12(Frame *dest, AVFrame *src) {
    int size = src->width * src->height;
    dest->offset = 0;
    dest->size = size * 3 / 2;
    memcpy(dest->data, src->data[0], size);
    memcpy(dest->data + size, src->data[1], size / 4);
    memcpy(dest->data + size + size / 4, src->data[2], size / 4);
}

void AsynVideoDecoder::copyNV12(Frame *dest, AVFrame *src) {
    int size = src->width * src->height;
    memcpy(dest->data, src->data[0], size);
    int uvSize = size / 4;
    for (int i = 0; i < uvSize; ++i) {
        *(dest->data + size + i) = src->data[1][i * 2];
        *(dest->data + size + uvSize + i) = src->data[1][i * 2 + 1];
    }
}

int AsynVideoDecoder::width() {
    if (decoder) {
        return decoder->width();
    }
    return 0;
}

int AsynVideoDecoder::height() {
    if (decoder) {
        return decoder->height();
    }
    return 0;
}

void AsynVideoDecoder::loop() {
    if (!lopping)
        return;
    pipeline->queueEvent([this] {
        if (!vRecycler)
            return;
        AVFrame *cacheFrame = vRecycler->takeCache();
        if (!cacheFrame) {
            return;
        }
        long long time = getCurrentTimeUS();
        int ret = decoder->grab(cacheFrame);
        LOGI("Grab frame(fmt:%d,type:%d) cost %lld, cache left %d, ret=%d",
             cacheFrame->format,
             cacheFrame->key_frame,
             (getCurrentTimeUS() - time),
             vRecycler->getCacheSize(), ret);

        if (MEDIA_TYPE_VIDEO == ret) {
            vRecycler->offer(cacheFrame);
        } else if (MEDIA_TYPE_AUDIO == ret) {
            vRecycler->offer(cacheFrame);
        } else {
            return;
        }
        loop();
    });
}

int AsynVideoDecoder::getChannels() {
    if (decoder) {
        return decoder->getChannels();
    }
    return 0;
}

int AsynVideoDecoder::getSampleHz() {
    if (decoder) {
        return decoder->getSampleHz();
    }
    return 0;
}

int AsynVideoDecoder::getSampleFormat() {
    if (decoder) {
        return decoder->getSampleFormat();
    }
    return 0;
}

int AsynVideoDecoder::getPerSampleSize() {
    if (decoder) {
        return decoder->getPerSampleSize();
    }
    return 0;
}

#ifdef __cplusplus
}
#endif