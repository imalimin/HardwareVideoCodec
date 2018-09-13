/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.pipeline

/**
 * Created by lmyooyo@gmail.com on 2018/6/20.
 */
class GLEventPipeline private constructor() : Pipeline {

    private object Holder {
        val INSTANCE: Pipeline = GLEventPipeline()
    }

    companion object {
        val INSTANCE: Pipeline by lazy { Holder.INSTANCE }
    }

    private var eventPipeline: EventPipeline = EventPipeline.create("GLEventPipeline")

    override fun queueEvent(event: Runnable) {
        eventPipeline.queueEvent(event)
    }

    override fun quit() {
        eventPipeline.quit()
    }

    override fun queueEvent(event: Runnable, delayed: Long) {
        eventPipeline.queueEvent(event, delayed)
    }

    override fun started(): Boolean {
        return eventPipeline.started()
    }

    override fun getName(): String {
        return eventPipeline.getName()
    }
}