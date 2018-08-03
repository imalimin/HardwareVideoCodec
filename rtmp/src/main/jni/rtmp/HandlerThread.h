/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "BlockQueue.h"
#include "Message.h"
#include "../../../../../codec/src/main/jni/codec/log.h"
#include <malloc.h>
#include <string.h>
#include <pthread.h>

#ifndef HARDWAREVIDEOCODEC_EVENTPIPELINE_H
#define HARDWAREVIDEOCODEC_EVENTPIPELINE_H


class HandlerThread {
public:
    HandlerThread();

    ~HandlerThread();

    void sendMessage(Message *msg);

    void sendMessageDelayed(Message *msg);

    void removeMessage(int what);

    void removeAllMessage(bool (*filter)(Message));

    Message popMessage();

    int size();

    bool started();

    void quit();

private:
    BlockQueue<Message> messageQueue;
    pthread_attr_t attr;
    pthread_t thread;
    bool running = true;
};


#endif //HARDWAREVIDEOCODEC_EVENTPIPELINE_H
