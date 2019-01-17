/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_LIST_H
#define HARDWAREVIDEOCODEC_LIST_H

#include "Collection.h"

template<class T>
class List : public Collection<T> {
public:
    virtual ~List() {
    }

    virtual void add(int index, T *e) = 0;

    virtual T *get(int index) = 0;

    virtual T *remove(int index) = 0;
};

#endif //HARDWAREVIDEOCODEC_LIST_H
