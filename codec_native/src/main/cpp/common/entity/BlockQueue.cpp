/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/BlockQueue.h"
#include "../include/log.h"

template<class T>
BlockQueue<T>::BlockQueue() {
    m_queue = new Queue();
    pthread_mutex_init(&mutex, nullptr);
    pthread_cond_init(&cond, nullptr);
}

template<class T>
BlockQueue<T>::~BlockQueue() {
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

template<class T>
bool BlockQueue<T>::offer(T *entity) {
    pthread_mutex_lock(&mutex);
//        if (size() >= SIZE_CACHE) {
//            pthread_cond_broadcast(cond);
//            pthread_mutex_unlock(mutex);
//            return false;
//        }

    m_queue->push_back(*entity);

    pthread_cond_broadcast(&cond);
    pthread_mutex_unlock(&mutex);
    return true;
}

template<class T>
T *BlockQueue<T>::take() {
    pthread_mutex_lock(&mutex);
    if (size() <= 0) {
        if (0 != pthread_cond_wait(&cond, &mutex)) {
            pthread_mutex_unlock(&mutex);
            return nullptr;
        }
    }
    T *e = nullptr;
    if (!isEmpty()) {
        e = &m_queue->front();
    }

    pthread_mutex_unlock(&mutex);
    return e;
}

template<class T>
void BlockQueue<T>::pop() {
    pthread_mutex_lock(&mutex);
    if (isEmpty())
        return;
    m_queue->pop_front();
    pthread_mutex_unlock(&mutex);
}

template<class T>
void BlockQueue<T>::clear() {
    pthread_cond_broadcast(&cond);
    pthread_mutex_lock(&mutex);
    m_queue->clear();
    pthread_mutex_unlock(&mutex);
}

template<class T>
int BlockQueue<T>::size() {
    return m_queue->size();
}

template<class T>
bool BlockQueue<T>::isEmpty() {
    return m_queue->empty();
}

template<class T>
void BlockQueue<T>::notify() {
    pthread_mutex_lock(&mutex);
    pthread_cond_broadcast(&cond);
    pthread_mutex_unlock(&mutex);
}