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

using namespace std;

#define SIZE_CACHE 200

template<class T>
class BlockQueue {
public:
    BlockQueue() {
        mutex = new pthread_mutex_t;
        cond = new pthread_cond_t;
        pthread_mutex_init(mutex, NULL);
        pthread_cond_init(cond, NULL);
    }

    ~BlockQueue() {
        pthread_mutex_lock(mutex);
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

        m_queue.push(entity);

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
        T e = m_queue.front();
        m_queue.pop();

        pthread_mutex_unlock(mutex);
        return e;
    }

    void clear() {
        pthread_mutex_lock(mutex);
        while (!isEmpty()) m_queue.pop();
        pthread_mutex_unlock(mutex);
    }

    int size() {
        return m_queue.size();
    }

    bool isEmpty() {
        return m_queue.empty();
    }

private:
    pthread_mutex_t *mutex;
    pthread_cond_t *cond;
    queue<T> m_queue;
};
