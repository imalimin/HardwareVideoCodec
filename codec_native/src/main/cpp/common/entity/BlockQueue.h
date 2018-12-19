//
// Created by limin on 2018/12/16.
//
#include <string>
#include <pthread.h>
#include <list>
#include "Object.h"

using namespace std;
#ifndef HARDWAREVIDEOCODEC_BLOCKQUEUE_H
#define HARDWAREVIDEOCODEC_BLOCKQUEUE_H

template<class T>
class BlockQueue : public Object {
public:
    typedef list<T> Queue;

    BlockQueue();

    ~BlockQueue();

    /**
     * 阻塞式向队列增加一个元素
     */
    bool offer(T *entity);

    /**
     * 阻塞式从队列拿出一个元素
     */
    T *take();

    /**
     * 阻塞式从队列删除一个元素
     */
    void pop();

    /**
     * 删除所有元素
     */
    void clear();

    /**
     * 获取队列大小
     */
    int size();

    /**
     * 检查队列是否为空
     */
    bool isEmpty();

    Iterator *getIterator();

private:
    pthread_mutex_t *mutex;
    pthread_cond_t *cond;
    Queue *m_queue;
};

template<class T>
class Iterator : public Object {
public:
    Iterator(list<T>::iterator iterator, list<T>::iterator end) {
        this->iterator = iterator;
    }

    ~Iterator() {
        this->iterator = nullptr;

    }

    T next() {
        return this->iterator++;
    }

    bool hasNext() {
        return end != this->iterator;
    }

private:
    list<T>::iterator iterator = nullptr;
    const list<T>::iterator end;
};


#endif //HARDWAREVIDEOCODEC_BLOCKQUEUE_H
