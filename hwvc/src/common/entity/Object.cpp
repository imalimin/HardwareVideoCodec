/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Object.h"
#include <sys/time.h>

Object::Object() {
    pthread_mutex_init(&mutex, nullptr);
    pthread_cond_init(&cond, nullptr);

}

Object::~Object() {
    pthread_mutex_destroy(&mutex);
    pthread_cond_destroy(&cond);
}

void Object::notify() {
    pthread_cond_broadcast(&cond);
}

void Object::wait() {
    pthread_mutex_lock(&mutex);
    if (0 != pthread_cond_wait(&cond, &mutex)) {
        pthread_mutex_unlock(&mutex);
    }
}

void Object::wait(int us) {
    if (us <= 0) return;
    struct timeval now;
    struct timespec waitTime;
    gettimeofday(&now, NULL);
    int sec = us / 1000000;
    int usec = us % 1000000;
    waitTime.tv_sec = now.tv_sec + sec;
    waitTime.tv_nsec = now.tv_usec * 1000 + usec * 1000;
    pthread_cond_timedwait(&cond, &mutex, &waitTime);
}