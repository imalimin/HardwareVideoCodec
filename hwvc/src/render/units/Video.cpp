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
    decoder = new Decoder();
}

Video::~Video() {

}

void Video::release() {
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
}

bool Video::eventPrepare(Message *msg) {
    decoder->prepare("/sdcard/001.mp4");
    return true;
}