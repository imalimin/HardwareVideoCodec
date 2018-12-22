/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Thread.h"
#include <assert.h>
#include "../include/log.h"

static void *run(void *arg) {
    Thread *thread = static_cast<Thread *>(arg);
    LOGI("Thread(%ld) start", (long) thread);
    thread->runnable();
    thread->stop();
    LOGI("Thread(%ld) stop", (long) thread);
    return nullptr;
}

Thread::Thread(string name, function<void()> runnable) {
    this->name = name;
    this->runnable = runnable;
}

Thread::~Thread() {
}

void Thread::start() {
    this->inter = false;
    createThread();
}

void Thread::createThread() {
    pthread_attr_init(&attr);
    //将线程的属性称为detached，则线程退出时会自己清理资源
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    int ret = pthread_create(&thread, &attr, run, (void *) this);
    if (0 != ret) {
        pthread_attr_destroy(&attr);
        LOGE("Thread create failed: %d", ret);
        assert(false);
    }
}

void Thread::stop() {
    pthread_attr_destroy(&attr);
}

bool Thread::isRunning() {
    return !inter;
}

void Thread::interrupt() {
    inter = true;
}

bool Thread::interrupted() {
    return inter;
}
