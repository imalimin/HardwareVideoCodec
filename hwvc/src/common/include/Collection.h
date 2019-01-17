/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_COLLECTION_H
#define HARDWAREVIDEOCODEC_COLLECTION_H

#include "Object.h"

template<class T>
class Collection : public Object {
public:
    virtual ~Collection() {
    }

    virtual bool add(T *e) = 0;

    virtual int contains(T *e) = 0;

    virtual bool isEmpty() = 0;

    virtual bool remove(T *e) = 0;

    virtual int size() = 0;
};

#endif //HARDWAREVIDEOCODEC_COLLECTION_H
