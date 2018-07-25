package com.lmy.codec.pipeline

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.loge

/**
 * Created by lmyooyo@gmail.com on 2018/6/21.
 */
class EventPipeline private constructor(name: String) {
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

    fun queueEvent(event: Runnable) {
        if (!start) {
            loge("SingleEventPipeline has quited")
            return
        }
        mHandler.sendMessage(mHandler.obtainMessage(0, event))
    }

    fun quit() {
        if (!start) {
            loge("SingleEventPipeline has quited")
            return
        }
        start = false
        mHandlerThread.interrupt()
        mHandlerThread.quitSafely()
    }

    fun started(): Boolean {
        return start
    }
}