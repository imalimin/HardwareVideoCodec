/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/Video.h"

Video::Video() {
    name = __func__;
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Video::eventPrepare));
    registerEvent(EVENT_VIDEO_START, reinterpret_cast<EventFunc>(&Video::eventStart));
    decoder = new Decoder();
    avFrame = av_frame_alloc();
}

Video::~Video() {
    release();
}

void Video::release() {
    if (avFrame) {
        av_frame_free(&avFrame);
        avFrame = nullptr;
    }
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
}

bool Video::eventPrepare(Message *msg) {
    decoder->prepare("/sdcard/001.mp4");
    return true;
}

bool Video::eventStart(Message *msg) {
    decoder->grab(avFrame);
    return true;
}