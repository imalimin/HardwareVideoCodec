/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Image.h"
#include "ObjectBox.h"

Image::Image() {
    name = __func__;
    decoder = new JpegDecoder();
}

Image::~Image() {

}

void Image::release() {
    Unit::release();
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    if (rgba) {
        delete[]rgba;
        rgba = nullptr;
    }
}

bool Image::dispatch(Message *msg) {
    Unit::dispatch(msg);
    switch (msg->what) {
        case EVENT_IMAGE_SHOW: {
            ObjectBox *ob = dynamic_cast<ObjectBox *>(msg->obj);
            show(static_cast<char *>(ob->ptr));
            delete[]ob->ptr;
            return true;
        }
        case EVENT_PIPELINE_RELEASE: {
            release();
            return true;
        }
        default:
            break;
    }
    return Unit::dispatch(msg);
}

void Image::show(string path) {
    if (rgba) {
        delete[]rgba;
        rgba = nullptr;
    }
    int width = 0, height = 0;
    bool ret = decoder->decodeFile(path, &rgba, &width, &height);
    if (!ret || 0 == width || 0 == height) {
        LOGE("Image decode %s failed", path.c_str());
        return;
    }
    LOGI("Image decode(%d x %d) %s", width, height, path.c_str());
    Message *msg = new Message(EVENT_PIPELINE_DRAW_SCREEN, nullptr);
    msg->obj = new ObjectBox(rgba);
    msg->arg1 = width;
    msg->arg2 = height;
    postEvent(msg);
}