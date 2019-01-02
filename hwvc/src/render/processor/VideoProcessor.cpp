/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/VideoProcessor.h"
#include "../include/Render.h"
#include "../include/Video.h"
#include "../include/Video.h"
#include "ObjectBox.h"

VideoProcessor::VideoProcessor() {
    pipeline = new UnitPipeline(__func__);
    pipeline->registerAnUnit(new Video());
    pipeline->registerAnUnit(new Render());
    pipeline->registerAnUnit(new Screen());
}

VideoProcessor::~VideoProcessor() {
    if (pipeline) {
        pipeline->release();
        delete pipeline;
        pipeline = nullptr;
    }
}

void VideoProcessor::prepare(ANativeWindow *win, int width, int height) {
    if (pipeline) {
        Message *msg = new Message(EVENT_COMMON_PREPARE, nullptr);
        msg->obj = new ObjectBox(win);
        msg->arg1 = width;
        msg->arg2 = height;
        pipeline->postEvent(msg);
    }
}

void VideoProcessor::start() {
    if (pipeline) {
        Message *msg = new Message(EVENT_PLAYER_START, nullptr);
        pipeline->postEvent(msg);
    }
}