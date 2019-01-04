/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_RECYCLERBLOCKQUEUE_H
#define HARDWAREVIDEOCODEC_RECYCLERBLOCKQUEUE_H

#include "Object.h"
#include "BlockQueue.h"

template<class T>
class RecyclerBlockQueue : public Object {
public:
    RecyclerBlockQueue() {
        queue = new BlockQueue<T>();
        recycler = new BlockQueue<T>();
    }

    ~RecyclerBlockQueue() {
        clear();
    }

    void clear() {
        if (recycler) {
            recycler->clear();
            delete recycler;
            recycler = nullptr;
        }
        if (queue) {
            queue->clear();
            delete queue;
            queue = nullptr;
        }
    }

    /**
     * 消费一个数据
     */
    T *take() {
        return queue->take();
    }

    /**
     * 提供一个数据
     */
    void offer(T *e) {
        queue->offer(e);
    }

    /*
     * 取出一个缓存
     */
    T *takeCache() {
        return recycler->take();
    }

    /**
     * 回收一个数据
     */
    void recycle(T *e) {
        recycler->offer(e);
    }

    int size() {
        return queue->size();
    }

    int getCacheSize() {
        return recycler->size();
    }

private:
    BlockQueue<T> *queue = nullptr;
    BlockQueue<T> *recycler = nullptr;
};

#endif //HARDWAREVIDEOCODEC_RECYCLERBLOCKQUEUE_H
