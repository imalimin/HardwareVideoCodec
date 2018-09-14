/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.pipeline.impl

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.loge
import com.lmy.codec.pipeline.Pipeline

/**
 * Created by lmyooyo@gmail.com on 2018/6/21.
 */
class EventPipeline private constructor(name: String) : Pipeline {

    companion object {
        fun create(name: String): EventPipeline {
            return EventPipeline(name)
        }
    }

    private var mHandlerThread: HandlerThread = HandlerThread(name)
    private var mHandler: Handler
    private var start = false

    init {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                val event = msg.obj
                (event as? Runnable)?.run()
            }
        }
        start = true
    }

    override fun queueEvent(event: Runnable) {
        if (!start) {
            loge("EventPipeline has quited")
            return
        }
        mHandler.sendMessage(mHandler.obtainMessage(0, event))
    }

    override fun queueEvent(event: Runnable, delayed: Long) {
        if (!start) {
            loge("EventPipeline has quited")
            return
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(0, event), delayed)
    }

    override fun quit() {
        if (!start) {
            loge("EventPipeline has quited")
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
}