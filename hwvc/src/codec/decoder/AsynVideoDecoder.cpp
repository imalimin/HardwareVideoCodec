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

AsynVideoDecoder::AsynVideoDecoder() {
    decoder = new DefaultVideoDecoder();
    vRecycler = new RecyclerBlockQueue<AVFrame>(8, [] {
        return av_frame_alloc();
    });
}

AsynVideoDecoder::~AsynVideoDecoder() {
    lopping = false;
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    if (vRecycler) {
        vRecycler->clear();
        delete vRecycler;
        vRecycler = nullptr;
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
    if (AV_PIX_FMT_NV12 == f->format) {
        copyNV12(frame, f);
    } else {
        copyYV12(frame, f);
    }

    frame->width = f->width;
    frame->height = f->height;
    vRecycler->recycle(f);

    return MEDIA_TYPE_VIDEO;
}

void AsynVideoDecoder::copyYV12(Frame *dest, AVFrame *src) {
    int size = src->width * src->height;
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
        loop();
        AVFrame *cacheFrame = vRecycler->takeCache();

        long long time = getCurrentTimeUS();
        int ret = decoder->grab(cacheFrame);
        LOGI("Grab frame(fmt:%d) cost %lld, cache left %d, ret=%d",
             cacheFrame->format,
             (getCurrentTimeUS() - time),
             vRecycler->getCacheSize(), ret);

        if (MEDIA_TYPE_VIDEO == ret) {
            vRecycler->offer(cacheFrame);
        } else {
            vRecycler->recycle(cacheFrame);
        }
    });
}

#ifdef __cplusplus
}
#endif