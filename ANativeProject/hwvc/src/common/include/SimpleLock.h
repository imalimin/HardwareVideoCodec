/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_SIMPLELOCK_H
#define HARDWAREVIDEOCODEC_SIMPLELOCK_H

#include "Object.h"

class SimpleLock : public Object {
public:
    SimpleLock();

    virtual ~SimpleLock();

    void lock();

    void unlock();

private:
    pthread_mutex_t mutex;
};


#endif //HARDWAREVIDEOCODEC_SIMPLELOCK_H
