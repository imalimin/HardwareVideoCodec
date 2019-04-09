/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/SimpleLock.h"

SimpleLock::SimpleLock() {
    pthread_mutex_init(&mutex, nullptr);
}

SimpleLock::~SimpleLock() {
    pthread_mutex_destroy(&mutex);
}

void SimpleLock::lock() {
    pthread_mutex_lock(&mutex);
}

void SimpleLock::unlock() {
    pthread_mutex_unlock(&mutex);
}