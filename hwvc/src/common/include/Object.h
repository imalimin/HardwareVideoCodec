/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_OBJECT_H
#define HARDWAREVIDEOCODEC_OBJECT_H


class Object {
public:
    Object();

    virtual ~Object();

    virtual void notify();
};


#endif //HARDWAREVIDEOCODEC_OBJECT_H
