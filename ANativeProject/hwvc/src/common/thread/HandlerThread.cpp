/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/HandlerThread.h"
#include "../include/log.h"

void HandlerThread::run() {
    while (true) {
        pthread_mutex_lock(&mutex);
        if (shouldQuit()) {
            break;
        }
        pthread_mutex_unlock(&mutex);
        Message *msg = this->take();
        int size = this->size();
        if (this->requestQuit && !this->requestQuitSafely) {
            LOGI("requestQuit, %ld, %ld", msg, pthread_self());
            break;
        }
        if (nullptr == msg) {
            continue;
        }
        msg->runnable(msg);
        int what = msg->what;
        if (1129270529 == msg->what) {
            LOGI("UnitPipeline(%s) release", this->name.c_str());
        }
        delete msg;
        if (this->requestQuitSafely && size <= 0) {
            LOGI("requestQuitSafely(%s) what=%d", this->name.c_str(), what);
            break;
        }
    }
    LOGI("HandlerThread(%s) quit", this->name.c_str());
}

HandlerThread::HandlerThread(string name) {
    this->name = name;
    pthread_mutex_init(&mutex, nullptr);
    queue = new MessageQueue();
    mThread = new thread(&HandlerThread::run, this);
}

bool HandlerThread::shouldQuit() {
    if (this->requestQuit && !this->requestQuitSafely) {
        return true;
    }
    return this->requestQuitSafely && size() <= 0;
}

HandlerThread::~HandlerThread() {
    quitSafely();
}

void HandlerThread::sendMessage(Message *msg) {
    if (requestQuitSafely || requestQuit) {
        LOGE("HandlerThread had quited");
        return;
    }
    offer(msg);
}

void HandlerThread::offer(Message *msg) {
    if (1129270529 == msg->what) {
        LOGI("UnitPipeline sendMessage");
    }
    queue->offer(msg);
}

Message *HandlerThread::take() {
    return queue->take();
}

int HandlerThread::size() {
    return queue->size();
}

void HandlerThread::pop() {
    queue->pop();
}

void HandlerThread::quit() {
    pthread_mutex_lock(&mutex);
    this->requestQuit = true;
    pthread_mutex_unlock(&mutex);
    if (nullptr != queue) {
        queue->notify();
    }
    if (nullptr != mThread) {
        mThread->join();
        delete mThread;
        mThread = nullptr;
    }
    if (this->queue) {
        delete this->queue;
        this->queue = nullptr;
    }
    pthread_mutex_destroy(&mutex);
}

void HandlerThread::quitSafely() {
    if (0 != size()) {
        this->requestQuitSafely = true;
    }
    quit();
}

void HandlerThread::removeAllMessage(int what) {
    queue->remove([what](Message *msg) {
        return what == msg->what;
    });
}