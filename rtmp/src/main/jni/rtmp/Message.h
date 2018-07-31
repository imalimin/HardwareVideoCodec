/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_EVENT_H
#define HARDWAREVIDEOCODEC_EVENT_H

class Message {
public:
    int what;
    int arg1;
    long arg2;
    void *obj;

    void (*handle)(Message *);

    Message(void (handle)(Message *));
};

Message *obtainMessage(int what, void *obj, void (handle)(Message *));

Message *obtainMessage(int what, int arg1, long arg2, void *obj, void (handle)(Message *));

#endif //HARDWAREVIDEOCODEC_EVENT_H
