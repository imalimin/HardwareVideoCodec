/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Image.h"
#include "ObjectBox.h"

Image::Image() {
    decoder = new JpegDecoder();
}

Image::~Image() {

}

void Image::release() {
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
}

bool Image::dispatch(Message *msg) {
    switch (msg->what) {
        case EVENT_IMAGE_SHOW: {
            ObjectBox *ob = dynamic_cast<ObjectBox *>(msg->obj);
            show(static_cast<char *>(ob->ptr));
            return true;
        }
        default:
            break;
    }
    return Unit::dispatch(msg);
}

void Image::show(string file) {
    uint8_t *rgba;
    int width = 0, height = 0;
    decoder->decodeFile("/sdcard/1.jpg", &rgba, &width, &height);
    if (0 == width || 0 == height) {
        LOGE("Image decode %s failed", file.c_str());
        return;
    }
    Message *msg = new Message(EVENT_PIPELINE_DRAW_SCREEN, nullptr);
    msg->obj = new ObjectBox(rgba);
    msg->arg1 = width;
    msg->arg2 = height;
    postEvent(msg);
}