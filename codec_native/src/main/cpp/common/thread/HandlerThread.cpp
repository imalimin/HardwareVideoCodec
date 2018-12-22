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
    thread = new Thread(name, [=]() {
        LOGI("HandlerThread run");
        while (thread->isRunning()) {
            LOGI("take");
            Message *msg = take();
            if (thread->interrupted()) {
                LOGI("take interrupted, %ld, %ld", msg, thread);
                break;
            }
            if (nullptr == msg) {
                pop();
                LOGI("take null, %ld, %ld", msg, thread);
                continue;
            }
            LOGI("take 2");
            msg->runnable(msg);
            LOGI("take 3");
            pop();
            LOGI("take 4");
            if (thread->interrupted()) {
                LOGI("take 2");
                break;
            }
        }
        LOGI("take 6");
        if (nullptr != queue) {
            delete queue;
            queue = nullptr;
        }
    });
    thread->start();
}

HandlerThread::~HandlerThread() {
    LOGI("~HandlerThread");
    if (nullptr != thread) {
        thread->interrupt();
    }
    if (nullptr != queue) {
        queue->notify();
    }
}

void HandlerThread::sendMessage(Message *msg) {
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