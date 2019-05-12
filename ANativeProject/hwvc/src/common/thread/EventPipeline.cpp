/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/EventPipeline.h"

EventPipeline::EventPipeline(string name) {
    this->handlerThread = new HandlerThread(name);
    this->shouldQuitThread = false;
}

EventPipeline::EventPipeline(HandlerThread *handlerThread) {
    this->handlerThread = handlerThread;
    this->shouldQuitThread = true;
}

EventPipeline::~EventPipeline() {
    notify();
    simpleLock.lock();
    if (shouldQuitThread && handlerThread) {
        delete handlerThread;
    }
    handlerThread = nullptr;
    simpleLock.unlock();
}

void EventPipeline::queueEvent(function<void()> event) {
    simpleLock.lock();
    if (handlerThread) {
        handlerThread->sendMessage(new Message(0, [event](Message *msg) {
            event();
        }));
    }
    simpleLock.unlock();
}