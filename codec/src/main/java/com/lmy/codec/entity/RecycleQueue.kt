/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.entity

import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by lmyooyo@gmail.com on 2018/5/21.
 */

abstract class RecycleQueue<T>(val capacity: Int) {
    private var queue: LinkedBlockingQueue<T>? = null
    private var cache: LinkedBlockingQueue<T>? = null

    val cacheSize: Int
        get() = synchronized(cache!!) {
            return cache!!.size
        }

    fun ready() {
        if (null == queue)
            queue = LinkedBlockingQueue(capacity)
        if (null == cache) {
            cache = LinkedBlockingQueue(capacity)
            for (i in 0 until capacity) {
                cache!!.offer(newCacheEntry())
            }
        }
    }

    fun offer(entry: T) {
        queue!!.offer(entry)
    }

    fun poll(): T {
        return queue!!.poll()
    }

    @Throws(InterruptedException::class)
    fun take(): T {
        return queue!!.take()
    }

    fun pollCache(): T {
        return cache!!.poll()
    }

    @Throws(InterruptedException::class)
    fun takeCache(): T {
        return cache!!.take()
    }

    @Synchronized
    fun recycle(entry: T) {
        cache!!.offer(entry)
    }

    abstract fun newCacheEntry(): T

    fun release() {
        queue!!.clear()
        cache!!.clear()
    }
}
