/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <string.h>
#include "Object.h"

#ifndef HARDWAREVIDEOCODEC_EVENT_H
#define HARDWAREVIDEOCODEC_EVENT_H

class Message {
public:
    int what;
    int arg1;
    long arg2;
    Object *obj;

    void (*handle)(Message *);

    Message(void (handle)(Message *));

    ~Message();
};

Message *obtainMessage(int what, Object *obj, void (handle)(Message *));

Message *obtainMessage(int what, int arg1, long arg2, Object *obj, void (handle)(Message *));

#endif //HARDWAREVIDEOCODEC_EVENT_H
