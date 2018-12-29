/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/MessageQueue.h"

MessageQueue::MessageQueue() {
    queue = new BlockQueue<Message>();
}

MessageQueue::~MessageQueue() {
    if (nullptr != queue) {
        delete queue;
        queue = nullptr;
    }
}

void MessageQueue::offer(Message *msg) {
    queue->offer(msg);
}

Message *MessageQueue::take() {
    return queue->take();
}

int MessageQueue::size() {
    return queue->size();
};

void MessageQueue::pop() {
    queue->pop();
}

void MessageQueue::notify() {
    queue->notify();
}
