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
            if (thread->interrupted()) break;
            LOGI("take");
            Message *msg = take();
            if (nullptr == msg) continue;
            msg->runnable(msg);
            pop();
        }
    });
    thread->start();
}

HandlerThread::~HandlerThread() {
    if (nullptr != queue) {
        delete queue;
        queue = nullptr;
    }
    if (nullptr != thread) {
        thread->interrupt();
        delete thread;
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