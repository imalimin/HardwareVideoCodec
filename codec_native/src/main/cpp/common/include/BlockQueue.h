/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
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

    virtual ~BlockQueue();

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

    virtual void notify() override;

private:
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    Queue *m_queue = nullptr;
};

#endif //HARDWAREVIDEOCODEC_BLOCKQUEUE_H
