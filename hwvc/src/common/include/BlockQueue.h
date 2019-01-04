/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_BLOCKQUEUE_H
#define HARDWAREVIDEOCODEC_BLOCKQUEUE_H

#include <string>
#include <pthread.h>
#include "LinkedStack.h"
#include "Object.h"
#include "log.h"

using namespace std;

template<class T>
class BlockQueue : public Object {
public:
    typedef LinkedStack<T> Queue;

    BlockQueue() {
        m_queue = new Queue();
        pthread_mutex_init(&mutex, nullptr);
        pthread_cond_init(&cond, nullptr);
    }

    virtual ~BlockQueue() {
        LOGI("~BlockQueue");
        pthread_mutex_lock(&mutex);
        if (nullptr != m_queue) {
            m_queue->clear();
            delete m_queue;
            m_queue = nullptr;
        }
        pthread_mutex_unlock(&mutex);
        pthread_mutex_destroy(&mutex);
        pthread_cond_destroy(&cond);
    }

    /**
     * 阻塞式向队列增加一个元素
     */
    bool offer(T *entity) {
        pthread_mutex_lock(&mutex);
//        if (size() >= SIZE_CACHE) {
//            pthread_cond_broadcast(cond);
//            pthread_mutex_unlock(mutex);
//            return false;
//        }

        m_queue->offer(entity);

        pthread_cond_broadcast(&cond);
        pthread_mutex_unlock(&mutex);
        return true;
    }

    /**
     * 阻塞式从队列拿出一个元素
     */
    T *take() {
        pthread_mutex_lock(&mutex);
        if (size() <= 0) {
            if (0 != pthread_cond_wait(&cond, &mutex)) {
                pthread_mutex_unlock(&mutex);
                return nullptr;
            }
        }
        T *e = nullptr;
        if (!isEmpty()) {
            e = m_queue->take();
        }

        pthread_mutex_unlock(&mutex);
        return e;
    }

    /**
     * 删除所有元素
     */
    void clear() {
        pthread_cond_broadcast(&cond);
        pthread_mutex_lock(&mutex);
        m_queue->clear();
        pthread_mutex_unlock(&mutex);
    }

    /**
     * 获取队列大小
     */
    int size() {
        return m_queue->size();
    }

    /**
     * 检查队列是否为空
     */
    bool isEmpty() {
        return m_queue->empty();
    }

    virtual void notify() override {
        Object::notify();
        pthread_mutex_lock(&mutex);
        pthread_cond_broadcast(&cond);
        pthread_mutex_unlock(&mutex);
    }

private:
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    Queue *m_queue = nullptr;
};

#endif //HARDWAREVIDEOCODEC_BLOCKQUEUE_H
