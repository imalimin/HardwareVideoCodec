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
    int size = f->width * f->height;
    memcpy(frame->data, f->data[0], size);
    memcpy(frame->data + size, f->data[1], size / 4);
    memcpy(frame->data + size + size / 4, f->data[2], size / 4);

    frame->width = f->width;
    frame->height = f->height;
    vRecycler->recycle(f);

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
        AVFrame *cacheFrame = vRecycler->takeCache();

        long long time = getCurrentTimeUS();
        int ret = decoder->grab(cacheFrame);
        LOGI("Grab cost %lld, cache left %d, ret=%d", (getCurrentTimeUS() - time),
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