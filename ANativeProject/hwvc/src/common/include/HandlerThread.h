/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <thread>
#include <pthread.h>
#include "MessageQueue.h"

#ifndef HARDWAREVIDEOCODEC_HANDLERTHREAD_H
#define HARDWAREVIDEOCODEC_HANDLERTHREAD_H

class HandlerThread : public Object {
public:
    HandlerThread(string name);

    virtual ~HandlerThread();

    void sendMessage(Message *msg);

    void removeAllMessage(int what);

    void quit();

    void quitSafely();

private:
    std::thread *mThread = nullptr;
    pthread_mutex_t mutex;
    MessageQueue *queue = nullptr;
    bool requestQuitSafely = false;
    bool requestQuit = false;

    void offer(Message *msg);

    Message *take();

    void pop();

    int size();

    bool shouldQuit();

    void run();
};


#endif //HARDWAREVIDEOCODEC_HANDLERTHREAD_H
