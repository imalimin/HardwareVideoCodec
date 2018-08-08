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
        Message *message = thiz->takeMessage();
        if (NULL == message) continue;
        if (WHAT_QUIT_SAFELY == message->what) {
            thiz->popMessage();
            break;
        }
        message->handle(message);
        thiz->popMessage();
    }
    thiz->quit();
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
    mutex = new pthread_mutex_t;
    cond = new pthread_cond_t;
    pthread_mutex_init(mutex, NULL);
    pthread_cond_init(cond, NULL);
}

HandlerThread::~HandlerThread() {
    quit();
}

void HandlerThread::sendMessage(Message *msg) {
    if (!started())
        return;
    messageQueue.offer(msg);
}

void HandlerThread::sendMessageDelayed(Message *msg) {
    if (!started())
        return;
    messageQueue.offer(msg);
}

void HandlerThread::quitSafely() {
    sendMessage(obtainMessage(WHAT_QUIT_SAFELY, NULL, NULL));
}

void HandlerThread::quit() {
    if (!started())
        return;
    running = false;
    messageQueue.clear();
    pthread_cond_broadcast(cond);
    pthread_attr_destroy(&attr);
    if (NULL != mutex) {
        pthread_mutex_destroy(mutex);
        mutex = NULL;
    }
    if (NULL != cond) {
        pthread_cond_destroy(cond);
        cond = NULL;
    }
    LOGI("RTMP: HandlerThread quit");
}

bool HandlerThread::started() {
    return running;
}

Message *HandlerThread::takeMessage() {
    return messageQueue.take();
}

void HandlerThread::popMessage() {
    messageQueue.pop();
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

void HandlerThread::removeAllMessage(short (*filter)(Message *)) {
    list<Message>::iterator it;
    for (it = messageQueue.begin(); it != messageQueue.end(); it++) {
        int result = filter(&*it);
        if (FILTER_REMOVE == result) {
            messageQueue.erase(it);
        } else if (FILTER_BREAK == result) {
            break;
        }
    }
}

int HandlerThread::sleep(long ms) {
    pthread_mutex_lock(mutex);
    struct timeval now;
    struct timespec outtime;
    gettimeofday(&now, NULL);
    outtime.tv_sec = now.tv_sec + ms / 1000;
    outtime.tv_nsec = (now.tv_usec + ms % 1000) * 1000;
    int ret = pthread_cond_timedwait(cond, mutex, &outtime);
    pthread_mutex_unlock(mutex);
    return ret;
}
