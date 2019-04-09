/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/EventPipeline.h"

EventPipeline::EventPipeline(string name) {
    pthread_mutex_init(&mutex, nullptr);
    pthread_cond_init(&cond, nullptr);
    this->handlerThread = new HandlerThread(name);
    this->shouldQuitThread = false;
}

EventPipeline::EventPipeline(HandlerThread *handlerThread) {
    pthread_mutex_init(&mutex, nullptr);
    pthread_cond_init(&cond, nullptr);
    this->handlerThread = handlerThread;
    this->shouldQuitThread = true;
}

EventPipeline::~EventPipeline() {
    notify();
    if (shouldQuitThread && handlerThread) {
        delete handlerThread;
    }
    handlerThread = nullptr;
    pthread_mutex_destroy(&mutex);
    pthread_cond_destroy(&cond);
}

void EventPipeline::queueEvent(function<void()> event) {
    if (handlerThread) {
        handlerThread->sendMessage(new Message(0, [event](Message *msg) {
            event();
        }));
    }
}