/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_OBJECT_H
#define HARDWAREVIDEOCODEC_OBJECT_H

#include "pthread.h"
//#include "semaphore.h"

class Object {
public:
    Object();

    virtual ~Object();

    virtual void wait();

    void wait(int us);

    virtual void notify();

private:
    pthread_mutex_t mutex;
    pthread_cond_t cond;
//    sem_t sem;
};


#endif //HARDWAREVIDEOCODEC_OBJECT_H
