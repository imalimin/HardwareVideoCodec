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
    LOGI("Thread(%ld) start", pthread_self());
    thread->runnable();
    thread->runnable = nullptr;
    thread->stop();
    LOGI("Thread(%ld) stop", pthread_self());
    return nullptr;
}

Thread::Thread(string name, function<void()> runnable) {
    this->inter = false;
    this->name = name;
    this->runnable = runnable;
    pthread_mutex_init(&mutex, nullptr);
    pthread_cond_init(&cond, nullptr);
}

Thread::~Thread() {
    LOGI("~Thread");
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
    pthread_mutex_destroy(&mutex);
    pthread_cond_destroy(&cond);
}

bool Thread::isRunning() {
    return !interrupted();
}

void Thread::interrupt() {
    lock();
    inter = true;
    unLock();
}

bool Thread::interrupted() {
    lock();
    bool ret = inter;
    unLock();
    return ret;
}

void Thread::lock() {
    if (NULL == &mutex)
        return;
    pthread_mutex_lock(&mutex);
}

void Thread::unLock() {
    if (NULL == &mutex)
        return;
    pthread_mutex_unlock(&mutex);
}
