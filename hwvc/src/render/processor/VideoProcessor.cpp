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
#include "../entity/NativeWindow.h"
#include "ObjectBox.h"
#include "String.h"

VideoProcessor::VideoProcessor() {
    pipeline = new UnitPipeline("VideoProcessor");
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

void VideoProcessor::setSource(char *path) {
    if (pipeline) {
        Message *msg = new Message(EVENT_VIDEO_SET_SOURCE, nullptr);
        msg->obj = new ObjectBox(path);
        pipeline->postEvent(msg);
    }
}

void VideoProcessor::prepare(ANativeWindow *win, int width, int height) {
    if (pipeline) {
        Message *msg = new Message(EVENT_COMMON_PREPARE, nullptr);
        msg->obj = new ObjectBox(new NativeWindow(win, nullptr));
        msg->arg1 = width;
        msg->arg2 = height;
        pipeline->postEvent(msg);
    }
}

void VideoProcessor::start() {
    if (pipeline) {
        Message *msg = new Message(EVENT_VIDEO_START, nullptr);
        pipeline->postEvent(msg);
    }
}

void VideoProcessor::pause() {
    if (pipeline) {
        Message *msg = new Message(EVENT_VIDEO_PAUSE, nullptr);
        pipeline->postEvent(msg);
    }
}

void VideoProcessor::seek(int64_t us) {
    if (pipeline) {
        pipeline->removeAllMessage(EVENT_VIDEO_SEEK);
        Message *msg = new Message(EVENT_VIDEO_SEEK, nullptr);
        msg->arg2 = us;
        pipeline->postEvent(msg);
    }
}

void VideoProcessor::setFilter(Filter *filter) {
    Message *msg = new Message(EVENT_RENDER_SET_FILTER, nullptr);
    msg->obj = new ObjectBox(filter);
    pipeline->postEvent(msg);
}