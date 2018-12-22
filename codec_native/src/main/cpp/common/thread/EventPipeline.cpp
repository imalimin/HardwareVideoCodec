/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/EventPipeline.h"

EventPipeline::EventPipeline(string name) {
    handlerThread = new HandlerThread(name);
}

EventPipeline::~EventPipeline() {
    if (nullptr != handlerThread) {
        delete handlerThread;
        handlerThread = nullptr;
    }
}

void EventPipeline::queueEvent(function<void()> event) {
    handlerThread->sendMessage(new Message(0, [=](Message *msg) {
        event();
    }));
}