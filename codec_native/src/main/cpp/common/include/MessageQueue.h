/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "BlockQueue.h"
#include "../entity/BlockQueue.cpp"
#include "Message.h"

#ifndef HARDWAREVIDEOCODEC_MESSAGEQUEUE_H
#define HARDWAREVIDEOCODEC_MESSAGEQUEUE_H

class MessageQueue : public Object {
public:
    MessageQueue();

    virtual ~MessageQueue();

    void offer(Message *msg);

    Message *take();

    int size();

    void pop();

    virtual void notify() override;

private:
    BlockQueue<Message> *queue = nullptr;
};

#endif //HARDWAREVIDEOCODEC_MESSAGEQUEUE_H
