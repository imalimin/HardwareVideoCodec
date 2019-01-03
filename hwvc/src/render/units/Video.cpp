/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/Video.h"
#include "ObjectBox.h"
#include "Size.h"

Video::Video() {
    name = __func__;
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Video::eventPrepare));
    registerEvent(EVENT_VIDEO_START, reinterpret_cast<EventFunc>(&Video::eventStart));
    pipeline = new EventPipeline(__func__);
    decoder = new Decoder();
    avFrame = av_frame_alloc();
}

Video::~Video() {
    release();
    LOGI("Video::~Image");
}

void Video::release() {
    Unit::release();
    LOGI("Video::release");
    if (avFrame) {
        av_frame_free(&avFrame);
        avFrame = nullptr;
    }
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    pipeline->queueEvent([=] {
        if (texAllocator) {
            delete texAllocator;
            texAllocator = nullptr;
        }
        if (egl) {
            delete egl;
            egl = nullptr;
        }
    });
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
}

bool Video::eventPrepare(Message *msg) {
    pipeline->wait();
    pipeline->queueEvent([=] {
        if (!egl) {
            egl = new Egl();
        }
        if (texAllocator) {
            texAllocator = new TextureAllocator();
        }
        decoder->prepare("/sdcard/001.mp4");
    });
    LOGI(__func__);
    return true;
}

bool Video::eventStart(Message *msg) {
    int ret = decoder->grab(avFrame);
    if (MEDIA_TYPE_VIDEO != ret) {
        LOGI("grab ret=%d", ret);
        return true;
    }
    if (!yuvFilter) {
        yuvFilter = new YUV420PFilter();
        yuvFilter->init(avFrame->width, avFrame->height);
        yuv[0] = texAllocator->alloc();
        yuv[1] = texAllocator->alloc();
        yuv[2] = texAllocator->alloc();
    }
    LOGI("grab %d x %d, ", avFrame->width, avFrame->height);
    glBindTexture(GL_TEXTURE_2D, yuv[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, avFrame->width, avFrame->height, 0, GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 avFrame->data[0]);
    glBindTexture(GL_TEXTURE_2D, yuv[1]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, avFrame->width / 2, avFrame->height / 2, 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 avFrame->data[1]);
    glBindTexture(GL_TEXTURE_2D, yuv[2]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, avFrame->width / 2, avFrame->height / 2, 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 avFrame->data[2]);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);

    glViewport(0, 0, avFrame->width, avFrame->height);
    yuvFilter->draw(yuv[0], yuv[1], yuv[2]);

    eventInvalidate(nullptr);
    return true;
}

bool Video::eventInvalidate(Message *m) {
    Message *msg = new Message(EVENT_RENDER_FILTER, nullptr);
    msg->obj = new ObjectBox(new Size(avFrame->width, avFrame->height));
    msg->arg1 = yuvFilter->getFrameBuffer()->getFrameTexture();
    postEvent(msg);
    return true;
}