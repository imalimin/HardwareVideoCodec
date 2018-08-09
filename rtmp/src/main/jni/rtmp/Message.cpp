/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "Message.h"

Message *obtainMessage(int what, Object *obj, void (handle)(Message *)) {
    Message *m = new Message(handle);
    m->what = what;
    m->obj = obj;
    return m;
}

Message *obtainMessage(int what, int arg1, long arg2, Object *obj, void (handle)(Message *)) {
    Message *m = new Message(handle);
    m->what = what;
    m->arg1 = arg1;
    m->arg2 = arg2;
    m->obj = obj;
    return m;
}

Message::Message(void (handle)(Message *)) {
    this->handle = handle;
}

Message::~Message() {
    this->handle = NULL;
    if (NULL != obj) {
        delete obj;
        obj = NULL;
    }
}
