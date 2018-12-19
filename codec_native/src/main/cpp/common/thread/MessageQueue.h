//
// Created by limin on 2018/12/16.
//

#ifndef HARDWAREVIDEOCODEC_MESSAGEQUEUE_H
#define HARDWAREVIDEOCODEC_MESSAGEQUEUE_H


#include "../entity/BlockQueue.h"
#include "Message.h"

class MessageQueue {
public:
    MessageQueue();

    ~MessageQueue();

    void offer(Message *msg);

    Message *take();

private:
    BlockQueue<Message> *queue = nullptr;
};


#endif //HARDWAREVIDEOCODEC_MESSAGEQUEUE_H
