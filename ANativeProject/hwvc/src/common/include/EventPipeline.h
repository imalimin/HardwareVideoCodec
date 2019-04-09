/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <functional>
#include <string>
#include "Object.h"
#include "HandlerThread.h"

#ifndef HARDWAREVIDEOCODEC_EVENTPIPELINE_H
#define HARDWAREVIDEOCODEC_EVENTPIPELINE_H

class EventPipeline : public Object {
public:
    EventPipeline(string name);

    EventPipeline(HandlerThread *handlerThread);

    virtual ~EventPipeline();

    void queueEvent(function<void()> event);

private:
    HandlerThread *handlerThread = nullptr;
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    bool shouldQuitThread = false;
};

#endif //HARDWAREVIDEOCODEC_EVENTPIPELINE_H
