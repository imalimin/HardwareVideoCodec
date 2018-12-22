/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Message.h"

Message::Message(int what, function<void(Message *msg)> runnable) {
    this->what = what;
    this->runnable = runnable;
}

Message::Message(int what, Object *obj, function<void(Message *msg)> runnable) {
    this->what = what;
    this->obj = obj;
    this->runnable = runnable;
}

Message::~Message() {
    this->runnable = nullptr;
    if (nullptr != obj) {
        delete obj;
        obj = nullptr;
    }
}