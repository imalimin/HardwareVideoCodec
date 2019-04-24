/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <functional>
#include "Object.h"

using namespace std;
#ifndef HARDWAREVIDEOCODEC_MESSAGE_H
#define HARDWAREVIDEOCODEC_MESSAGE_H

class Message : public Object {
public:
    function<void(Message *msg)> runnable = nullptr;
    int what;
    int arg1;
    int64_t arg2;
    Object *obj = nullptr;

    Message(int what, function<void(Message *msg)> runnable);

    Message(int what, Object *obj, function<void(Message *msg)> runnable);

    virtual ~Message();

    void *tyrUnBox();
};

#endif //HARDWAREVIDEOCODEC_MESSAGE_H
