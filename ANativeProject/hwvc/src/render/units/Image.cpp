/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Image.h"
#include "ObjectBox.h"
#include "Size.h"
#include "Logcat.h"

Image::Image() {
    name = __FUNCTION__;
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Image::eventPrepare));
    registerEvent(EVENT_IMAGE_SHOW, reinterpret_cast<EventFunc>(&Image::eventShow));
    registerEvent(EVENT_COMMON_INVALIDATE, reinterpret_cast<EventFunc>(&Image::eventInvalidate));
}

Image::~Image() {
}

bool Image::eventRelease(Message *msg) {
    Logcat::i("HWVC", "Image::eventRelease");
    if (texAllocator) {
        delete texAllocator;
        texAllocator = nullptr;
    }
    if (hwBitmap) {
        delete hwBitmap;
        hwBitmap = nullptr;
    }
    return true;
}

void Image::show(string path) {
    if (!decode(path) && !hwBitmap) {
        return;
    }
    tex = texAllocator->alloc(hwBitmap->getPixels(), hwBitmap->getWidth(), hwBitmap->getHeight());
    eventInvalidate(nullptr);
}

bool Image::decode(string path) {
    if (hwBitmap) {//HwBitmap暂时不支持复用，所以先删除
        delete[]hwBitmap;
        hwBitmap = nullptr;
    }
    hwBitmap = HwBitmapFactory::decodeFile(path);
    if (!hwBitmap) {
        Logcat::i("HWVC", "Image decode %s failed", path.c_str());
        return false;
    }
    Logcat::i("HWVC", "Image decode(%d x %d) %s",
              hwBitmap->getWidth(),
              hwBitmap->getHeight(),
              path.c_str());
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
        msg->obj = new ObjectBox(new Size(hwBitmap->getWidth(), hwBitmap->getHeight()));
        msg->arg1 = tex;
        postEvent(msg);
    }
    return true;
}