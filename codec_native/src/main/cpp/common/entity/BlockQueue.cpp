//
// Created by limin on 2018/12/16.
//

#include "BlockQueue.h"

template<class T>
typedef typename list<T>::iterator Iterator;

BlockQueue::BlockQueue() {
    m_queue = new Queue();
    mutex = new pthread_mutex_t;
    cond = new pthread_cond_t;
    pthread_mutex_init(mutex, NULL);
    pthread_cond_init(cond, NULL);
}

BlockQueue::~BlockQueue() {
    pthread_mutex_lock(mutex);
    pthread_mutex_unlock(mutex);
    if (NULL != m_queue) {
        delete m_queue;
        m_queue = NULL;
    }
    pthread_mutex_destroy(mutex);
    pthread_cond_destroy(cond);
}

template<class T>
bool BlockQueue::offer(T *entity) {
    pthread_mutex_lock(mutex);
//        if (size() >= SIZE_CACHE) {
//            pthread_cond_broadcast(cond);
//            pthread_mutex_unlock(mutex);
//            return false;
//        }

    m_queue->push_back(*entity);

    pthread_cond_broadcast(cond);
    pthread_mutex_unlock(mutex);
    return true;
}

template<class T>
T *BlockQueue::take() {
    pthread_mutex_lock(mutex);
    while (size() <= 0) {
        if (0 != pthread_cond_wait(cond, mutex)) {
            pthread_mutex_unlock(mutex);
            return NULL;
        }
    }
    T *e = NULL;
    if (!isEmpty()) {
        e = &m_queue->front();
    }

    pthread_mutex_unlock(mutex);
    return e;
}

void BlockQueue::pop() {
    pthread_mutex_lock(mutex);
    m_queue->pop_front();
    pthread_mutex_unlock(mutex);
}

void BlockQueue::clear() {
    pthread_cond_broadcast(cond);
    pthread_mutex_lock(mutex);
    m_queue->clear();
    pthread_mutex_unlock(mutex);
}

int BlockQueue::size() {
    return m_queue->size();
}

bool BlockQueue::isEmpty() {
    return m_queue->empty();
}

Iterator BlockQueue::begin() {
    pthread_mutex_lock(mutex);
    Iterator it = m_queue->begin();
    pthread_mutex_unlock(mutex);
    return it;
}

Iterator BlockQueue::end() {
    pthread_mutex_lock(mutex);
    Iterator it = m_queue->end();
    pthread_mutex_unlock(mutex);
    return it;
}

void BlockQueue::erase(Iterator iterator) {
    pthread_mutex_lock(mutex);
    m_queue->erase(iterator);
    pthread_mutex_unlock(mutex);
}