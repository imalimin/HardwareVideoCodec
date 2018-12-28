/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/HandlerThread.h"
#include "../include/log.h"

HandlerThread::HandlerThread(string name) {
    queue = new MessageQueue();
    thread = new Thread(name, [this]() {
        while (this->thread->isRunning()) {
            Message *msg = this->take();
            if (this->requestQuit && !this->requestQuitSafely) {
                LOGI("requestQuit, %ld, %ld", msg, this->thread);
                break;
            }
            if (nullptr == msg) {
                this->pop();
                LOGI("take null, %ld, %ld", msg, this->thread);
                continue;
            }
            msg->runnable(msg);
            this->pop();
            if (this->requestQuitSafely && 0 == this->size()) {
                LOGI("requestQuitSafely");
                break;
            }
        }
        if (this->queue) {
            delete this->queue;
            this->queue = nullptr;
        }
        if (this->thread) {
            delete this->thread;
            this->thread = nullptr;
        }
    });
    thread->start();
}

HandlerThread::~HandlerThread() {
    LOGI("~HandlerThread");
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
    this->requestQuit = true;
    if (nullptr != thread) {
        thread->interrupt();
    }
    if (nullptr != queue) {
        queue->notify();
    }
}

void HandlerThread::quitSafely() {
    if (0 != size()) {
        this->requestQuitSafely = true;
    }
    quit();
}