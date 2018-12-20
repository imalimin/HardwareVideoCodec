/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "Thread.h"
#include "../entity/Object.h"
#include "MessageQueue.h"

#ifndef HARDWAREVIDEOCODEC_HANDLERTHREAD_H
#define HARDWAREVIDEOCODEC_HANDLERTHREAD_H

class HandlerThread : public Object {
public:
    HandlerThread(string name);

    virtual ~HandlerThread();

    void sendMessage(Message *msg);

private:
    bool running = true;
    pthread_attr_t attr;
    Thread *thread = nullptr;
    MessageQueue *queue = nullptr;

    void offer(Message *msg);

    Message *take();

    void run(void *thiz);
};


#endif //HARDWAREVIDEOCODEC_HANDLERTHREAD_H
