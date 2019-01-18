/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Image.h"
#include "ObjectBox.h"
#include "Size.h"

Image::Image() {
    name = __func__;
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Image::eventPrepare));
    registerEvent(EVENT_IMAGE_SHOW, reinterpret_cast<EventFunc>(&Image::eventShow));
    registerEvent(EVENT_COMMON_INVALIDATE, reinterpret_cast<EventFunc>(&Image::eventInvalidate));
    decoder = new JpegDecoder();
    pDecoder = new PngDecoder();
}

Image::~Image() {
    LOGI("Image::~Image");
}

bool Image::eventRelease(Message *msg) {
    LOGI("Image::eventRelease");
    if (pDecoder) {
        delete pDecoder;
        pDecoder = nullptr;
    }
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    if (texAllocator) {
        delete texAllocator;
        texAllocator = nullptr;
    }
    if (rgba) {
        delete[]rgba;
        rgba = nullptr;
    }
    return true;
}

void Image::show(string path) {
    if (!decode(path)) {
        return;
    }
    tex = texAllocator->alloc(rgba, width, height);
    eventInvalidate(nullptr);
}

bool Image::decode(string path) {
    if (rgba) {
        delete[]rgba;
        rgba = nullptr;
    }
    int ret = 0;
    ret = pDecoder->decodeFile(path, &rgba, &width, &height);
    if (ret <= 0) {
        ret = decoder->decodeFile(path, &rgba, &width, &height);
    }
    if (!ret || 0 == width || 0 == height) {
        LOGE("Image decode %s failed", path.c_str());
        return false;
    }
    LOGI("Image decode(%d x %d) %s", width, height, path.c_str());
    return true;
}

bool Image::eventPrepare(Message *msg) {
    texAllocator = new TextureAllocator();
    return true;
}

bool Image::eventShow(Message *msg) {
    char *path = static_cast<char *>(msg->tyrUnBox());
    show(path);
    delete[]path;
    return true;
}

bool Image::eventInvalidate(Message *m) {
    if (GL_NONE != tex) {
        Message *msg = new Message(EVENT_RENDER_FILTER, nullptr);
        msg->obj = new ObjectBox(new Size(width, height));
        msg->arg1 = tex;
        postEvent(msg);
    }
    return true;
}