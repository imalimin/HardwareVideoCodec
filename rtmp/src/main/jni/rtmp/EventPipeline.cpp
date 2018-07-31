/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "EventPipeline.h"

static void *run(void *arg) {
    EventPipeline *thiz = (EventPipeline *) arg;
    while (thiz->started()) {
        LOGI("handle");
        Message message = thiz->messageQueue.take();
        message.handle(&message);
        LOGI("finish");
    }
    return NULL;
}

EventPipeline::EventPipeline() {
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

EventPipeline::~EventPipeline() {
    quit();
}

void EventPipeline::queueEvent(Message *msg) {
    if (!started())
        return;
    messageQueue.offer(*msg);
}

void EventPipeline::queueEventDelayed(Message *msg) {
    if (!started())
        return;
    messageQueue.offer(*msg);
}

void EventPipeline::quit() {
    if (!started())
        return;
    running = false;
    pthread_attr_destroy(&attr);
}

bool EventPipeline::started() {
    return running;
}
