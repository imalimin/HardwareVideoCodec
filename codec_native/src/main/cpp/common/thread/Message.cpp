/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Message.h"
#include "../include/ObjectBox.h"

Message::Message(int what, function<void(Message *msg)> runnable) {
    this->what = what;
    this->obj = nullptr;
    this->runnable = runnable;
    this->arg1 = 0;
    this->arg2 = 0;
}

Message::Message(int what, Object *obj, function<void(Message *msg)> runnable) {
    this->what = what;
    this->obj = obj;
    this->runnable = runnable;
    this->arg1 = 0;
    this->arg2 = 0;
}

Message::~Message() {
    this->runnable = nullptr;
    if (obj) {
        delete obj;
        obj = nullptr;
    }
}

void *Message::tyrUnBox() {
    ObjectBox *ob = dynamic_cast<ObjectBox *>(obj);
    return ob->ptr;
}