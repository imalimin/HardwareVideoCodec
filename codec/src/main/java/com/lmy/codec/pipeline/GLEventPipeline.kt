package com.lmy.codec.pipeline

import com.lmy.codec.loge

/**
 * Created by lmyooyo@gmail.com on 2018/6/20.
 */
class GLEventPipeline private constructor() {
    private object Holder {
        val INSTANCE = GLEventPipeline()
    }

    companion object {
        val INSTANCE: GLEventPipeline by lazy { Holder.INSTANCE }
    }

    private lateinit var eventPipeline: EventPipeline
    private var start = false

    fun start() {
        if (start) {
            loge("GLEventPipeline has started")
            return
        }
        eventPipeline = EventPipeline.create("GLEventPipeline")
        start = true
    }

    fun queueEvent(event: Runnable) {
        if (!start) {
            loge("GLEventPipeline has quited. Please call GLEventPipeline.INSTANCE.start() before.")
            return
        }
        eventPipeline.queueEvent(event)
    }

    fun quit() {
        if (!start) {
            loge("GLEventPipeline has quited")
            return
        }
        start = false
        eventPipeline.quit()
    }
}