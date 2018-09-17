/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.pipeline.impl

import android.os.Handler
import com.lmy.codec.loge
import com.lmy.codec.pipeline.Pipeline

/**
 * Created by lmyooyo@gmail.com on 2018/6/20.
 */
class GLEventPipeline private constructor() : Pipeline {

    private object Holder {
        val INSTANCE: Pipeline = GLEventPipeline()
    }

    companion object {
        val INSTANCE: Pipeline by lazy { Holder.INSTANCE }
        fun isMe(pipeline: Pipeline): Boolean {
            return INSTANCE == pipeline
        }
    }

    private var eventPipeline: EventPipeline? = null
    private fun check() {
        if (null == eventPipeline)
            eventPipeline = EventPipeline.create("GLEventPipeline")
    }

    override fun queueEvent(event: Runnable) {
        check()
        eventPipeline?.queueEvent(event)
    }

    override fun queueEvent(event: Runnable, delayed: Long) {
        check()
        eventPipeline?.queueEvent(event, delayed)
    }

    override fun quit() {
        if (null != eventPipeline) {
            eventPipeline?.quit()
            eventPipeline = null
            loge("GLEventPipeline quited")
        }
    }

    override fun started(): Boolean {
        if (null == eventPipeline) return false
        loge("GLEventPipeline started")
        return eventPipeline!!.started()
    }

    override fun getName(): String {
        if (null == eventPipeline) return "UNKNOWN"
        return eventPipeline!!.getName()
    }

    override fun getHandler(): Handler {
        return eventPipeline!!.getHandler()
    }
}