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
    cache = new BlockQueue<AVFrame>();
    recycler = new BlockQueue<AVFrame>();
    decoder = new DefaultVideoDecoder();
    for (int i = 0; i < 3; ++i) {
        AVFrame *frame = av_frame_alloc();
        recycler->offer(frame);
    }
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
    if (recycler) {
        recycler->clear();
        delete recycler;
        recycler = nullptr;
    }
    if (cache) {
        cache->clear();
        delete cache;
        cache = nullptr;
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
    AVFrame *cacheFrame = cache->take();
    int size = cacheFrame->width * cacheFrame->height;
    memcpy(frame->data, cacheFrame->data[0], size);
    memcpy(frame->data + size, cacheFrame->data[1], size / 4);
    memcpy(frame->data + size + size / 4, cacheFrame->data[2], size / 4);
    frame->width = cacheFrame->width;
    frame->height = cacheFrame->height;

    recycler->offer(av_frame_alloc());

    return MEDIA_TYPE_VIDEO;
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
        AVFrame *cacheFrame = recycler->take();

        long long time = getCurrentTimeUS();
        int ret = decoder->grab(cacheFrame);
        LOGI("Grab cost %lld, cache left %d", (getCurrentTimeUS() - time), recycler->size());

        if (MEDIA_TYPE_VIDEO == ret) {
            cache->offer(cacheFrame);
        } else {
            recycler->offer(cacheFrame);
        }
    });
}

#ifdef __cplusplus
}
#endif