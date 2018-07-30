package com.lmy.codec.pipeline

import com.lmy.codec.loge

/**
 * Created by lmyooyo@gmail.com on 2018/6/20.
 */
class SingleEventPipeline private constructor() {
    private object Holder {
        val INSTANCE = SingleEventPipeline()
    }

    companion object {
        val instance: SingleEventPipeline by lazy { Holder.INSTANCE }
    }

    private lateinit var eventPipeline: EventPipeline
    private var start = false

    fun start() {
        if (start) {
            loge("SingleEventPipeline has started")
            return
        }
        eventPipeline = EventPipeline.create("SingleEventPipeline")
        start = true
    }

    fun queueEvent(event: Runnable) {
        if (!start) {
            loge("SingleEventPipeline has quited. Please call SingleEventPipeline.instance.start() before.")
            return
        }
        eventPipeline.queueEvent(event)
    }

    fun quit() {
        if (!start) {
            loge("SingleEventPipeline has quited")
            return
        }
        start = false
        eventPipeline.quit()
    }
}