/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/HandlerThread.h"
#include "../include/log.h"

HandlerThread::HandlerThread(string name) {
    pthread_mutex_init(&mutex, nullptr);
    queue = new MessageQueue();
    thread = new Thread(name, [this]() {
        while (this->thread->isRunning()) {
            pthread_mutex_lock(&mutex);
            if (shouldQuit()) {
                break;
            }
            pthread_mutex_unlock(&mutex);
            Message *msg = this->take();
            int size = this->size();
            if (this->requestQuit && !this->requestQuitSafely) {
                LOGI("requestQuit, %ld, %ld", msg, this->thread);
                break;
            }
            if (nullptr == msg) {
                continue;
            }
            msg->runnable(msg);
            delete msg;
            if (this->requestQuitSafely && size <= 0) {
                LOGI("requestQuitSafely");
                break;
            }
        }
    });
    thread->start();
}

bool HandlerThread::shouldQuit() {
    if (thread->interrupted()) {
        return true;
    }
    if (this->requestQuit && !this->requestQuitSafely) {
        return true;
    }
    if (this->requestQuitSafely && size() <= 0) {
        return true;
    }
    return false;

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
    if (nullptr != thread) {
        thread->interrupt();
        delete thread;
        thread = nullptr;
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