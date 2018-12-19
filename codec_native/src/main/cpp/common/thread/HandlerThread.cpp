/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "HandlerThread.h"
#include "../log.h"

void HandlerThread::run(void *thiz) {

}

HandlerThread::HandlerThread(string name) {
    running = true;
    queue = new MessageQueue();
    thread = new Thread(name, [=]() {
        while (running) {
            Message *msg = take();
            msg->runnable(msg);
            delete msg;
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