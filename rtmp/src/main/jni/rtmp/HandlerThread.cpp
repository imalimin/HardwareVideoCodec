/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "HandlerThread.h"

static void *run(void *arg) {
    HandlerThread *thiz = (HandlerThread *) arg;
    while (thiz->started()) {
        Message message = thiz->popMessage();
        message.handle(&message);
        //TODO:会闪退
//        delete &message;
    }
    return NULL;
}

HandlerThread::HandlerThread() {
    running = true;
    pthread_attr_init(&attr);
    //将线程的属性称为detached，则线程退出时会自己清理资源
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    int ret = pthread_create(&thread, &attr, run, (void *) this);
    if (0 != ret) {
        pthread_attr_destroy(&attr);
        LOGE("Pthread create failed: %d", ret);
    }
}

HandlerThread::~HandlerThread() {
    quit();
}

void HandlerThread::sendMessage(Message *msg) {
    if (!started())
        return;
    messageQueue.offer(*msg);
}

void HandlerThread::sendMessageDelayed(Message *msg) {
    if (!started())
        return;
    messageQueue.offer(*msg);
}

void HandlerThread::quit() {
    if (!started())
        return;
    running = false;
    pthread_attr_destroy(&attr);
}

bool HandlerThread::started() {
    return running;
}

Message HandlerThread::popMessage() {
    return messageQueue.take();
}

int HandlerThread::size() {
    return messageQueue.size();
}

void HandlerThread::removeMessage(int what) {
    list<Message>::iterator it;
    for (it = messageQueue.begin(); it != messageQueue.end(); it++) {
        if (what == (*it).what) {
            messageQueue.erase(it);
            break;
        }
    }
}

void HandlerThread::removeAllMessage(bool (*filter)(Message)) {
    list<Message>::iterator it;
    for (it = messageQueue.begin(); it != messageQueue.end(); it++) {
        if (filter(*it)) {
            messageQueue.erase(it);
        }
    }
}
