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
    handlerThread = new HandlerThread(name);
}

EventPipeline::~EventPipeline() {
    notify();
    if (nullptr != handlerThread) {
        delete handlerThread;
        handlerThread = nullptr;
    }
    pthread_mutex_destroy(&mutex);
    pthread_cond_destroy(&cond);
}

void EventPipeline::queueEvent(function<void()> event) {
    handlerThread->sendMessage(new Message(0, [=](Message *msg) {
        event();
    }));
}

void EventPipeline::wait() {
    Object::wait();
    pthread_mutex_lock(&mutex);
    if (0 != pthread_cond_wait(&cond, &mutex)) {
        pthread_mutex_unlock(&mutex);
    }
}

void EventPipeline::notify() {
    Object::notify();
    pthread_mutex_lock(&mutex);
    pthread_cond_broadcast(&cond);
    pthread_mutex_unlock(&mutex);
};