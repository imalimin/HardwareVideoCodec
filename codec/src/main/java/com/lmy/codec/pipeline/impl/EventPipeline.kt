/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.pipeline.impl

import android.os.Handler
import android.os.HandlerThread
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.util.debug_e

/**
 * Created by lmyooyo@gmail.com on 2018/6/21.
 */
class EventPipeline private constructor(name: String) : Pipeline {

    companion object {
        fun create(name: String): EventPipeline {
            return EventPipeline(name)
        }
    }

    private val lock = Object()
    private var start = false
    private var mHandlerThread: HandlerThread = HandlerThread(name).apply {
        start()
        start = true
    }
    private var mHandler = Handler(mHandlerThread.looper)

    override fun queueEvent(event: Runnable, front: Boolean) {
        if (!start) {
            debug_e("EventPipeline has quited")
            return
        }
        if (front) {
            mHandler.postAtFrontOfQueue(event)
        } else {
            mHandler.post(event)
        }

    }

    override fun queueEvent(event: Runnable, delayed: Long) {
        if (!start) {
            debug_e("EventPipeline has quited")
            return
        }
        mHandler.postDelayed(event, delayed)
    }

    override fun quit() {
        if (!start) {
            debug_e("EventPipeline has quited")
            return
        }
        start = false
        mHandlerThread.interrupt()
        mHandlerThread.quitSafely()
    }

    override fun started(): Boolean {
        return start
    }

    override fun getName(): String {
        return mHandlerThread.name
    }

    override fun getHandler(): Handler {
        return mHandler
    }

    override fun sleep() {
        queueEvent(Runnable {
            synchronized(lock) {
                lock.wait()
            }
        })
    }

    override fun wake() {
        synchronized(lock) {
            lock.notifyAll()
        }
    }
}