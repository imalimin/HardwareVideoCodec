//
// Created by limin on 2018/8/9.
//
#include <pthread.h>

#ifndef HARDWAREVIDEOCODEC_LOCK_H
#define HARDWAREVIDEOCODEC_LOCK_H


class Lock {
public:
    Lock();
    ~Lock();
    int lock();
    int unlock();
private:
    pthread_mutex_t *mutex;
};


#endif //HARDWAREVIDEOCODEC_LOCK_H
