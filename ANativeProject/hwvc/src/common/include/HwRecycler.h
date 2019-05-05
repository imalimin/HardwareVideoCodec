/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWRECYCLER_H
#define HARDWAREVIDEOCODEC_HWRECYCLER_H

#include "Object.h"

template<class T>
class HwRecycler : public Object {
public:
    HwRecycler() {
    }

    virtual ~HwRecycler() {
        clear();
    }

    virtual void clear() = 0;

    virtual T *take() = 0;

    virtual void offer(T *e) = 0;

    virtual T *takeCache() = 0;

    virtual void recycle(T *e) = 0;

    virtual void recycleAll() = 0;

    virtual int size() = 0;

    virtual int getCacheSize() = 0;
};

#endif //HARDWAREVIDEOCODEC_HWRECYCLER_H
