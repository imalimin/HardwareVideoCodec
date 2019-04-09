/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <pthread.h>
#include <string>
#include <functional>
#include "Object.h"

using namespace std;
#ifndef HARDWAREVIDEOCODEC_THREAD_H
#define HARDWAREVIDEOCODEC_THREAD_H


class Thread : public Object {
public:
    string name;
    function<void()> runnable;

    Thread(string name, function<void()> runnable);

    void start();

    bool isRunning();

    void interrupt();

    bool interrupted();

    virtual ~Thread();

    void stop();

private:
    pthread_attr_t attr;
    pthread_t thread;
    pthread_mutex_t mutex;
    bool inter = false;

    void createThread();

    void lock();

    void unLock();
};


#endif //HARDWAREVIDEOCODEC_THREAD_H
