//
// Created by limin on 2018/8/9.
//

#include "Lock.h"

Lock::Lock() {
    mutex = new pthread_mutex_t;
    pthread_mutex_init(mutex, NULL);
}

Lock::~Lock() {
    if (NULL != mutex) {
        pthread_mutex_destroy(mutex);
        mutex = NULL;
    }
}

int Lock::lock() {
    return pthread_mutex_lock(mutex);
}

int Lock::unlock() {
    return pthread_mutex_unlock(mutex);
}
