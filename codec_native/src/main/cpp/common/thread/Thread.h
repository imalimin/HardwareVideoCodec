/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <pthread.h>
#include <string>
#include <functional>
#include "../entity/Object.h"

using namespace std;
#ifndef HARDWAREVIDEOCODEC_THREAD_H
#define HARDWAREVIDEOCODEC_THREAD_H


class Thread : public Object {
public:
    function<void()> runnable;

    Thread(string name, function<void()> runnable);

    void start();

    bool isRunning();

    void interrupt();

    ~Thread();

private:
    string name;
    pthread_attr_t attr;
    pthread_t thread;
    bool running = false;

    void createThread();
};


#endif //HARDWAREVIDEOCODEC_THREAD_H
