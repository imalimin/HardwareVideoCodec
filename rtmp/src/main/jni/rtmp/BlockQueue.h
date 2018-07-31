/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../../../../../codec/src/main/jni/codec/log.h"
#include <string.h>
#include <pthread.h>
#include <queue>

#define SIZE_CACHE 200

template<class T>
class BlockQueue {
public:
    BlockQueue() {
        _size = 0;
        queue = new T[SIZE_CACHE];
        mutex = new pthread_mutex_t;
        cond = new pthread_cond_t;
        pthread_mutex_init(mutex, NULL);
        pthread_cond_init(cond, NULL);
    }

    ~BlockQueue() {
        pthread_mutex_lock(mutex);
        if (queue != NULL)
            delete queue;
        pthread_mutex_unlock(mutex);

        pthread_mutex_destroy(mutex);
        pthread_cond_destroy(cond);
    }

    bool offer(T entity) {
        pthread_mutex_lock(mutex);
        if (size() >= SIZE_CACHE) {
            pthread_cond_broadcast(cond);
            pthread_mutex_unlock(mutex);
            return false;
        }

//        m_back = (m_back + 1) % m_max_size;
//        m_array[m_back] = item;
//
//        m_size++;
        pthread_cond_broadcast(cond);
        pthread_mutex_unlock(mutex);

        return true;
    }

    T take() {
        pthread_mutex_lock(mutex);
        while (size() <= 0) {
            if (0 != pthread_cond_wait(cond, mutex)) {
                pthread_mutex_unlock(mutex);
                return NULL;
            }
        }

//        m_front = (m_front + 1) % m_max_size;
//        item = m_array[m_front];
//        m_size--;
        pthread_mutex_unlock(mutex);
        return true;
        return NULL;
    }

    void clear() {
        pthread_mutex_lock(mutex);
        pthread_mutex_unlock(mutex);
    }

    int size() {
        return _size;
    }

    bool isEmpty() {
        return 0 == size();
    }

private:
    pthread_mutex_t *mutex;
    pthread_cond_t *cond;
    T *queue;
    int _size;
};
