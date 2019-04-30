package com.lmy.hwvcnative.processor

import android.view.Surface
import com.lmy.hwvcnative.CPPObject
import com.lmy.hwvcnative.FilterSupport
import com.lmy.hwvcnative.filter.Filter

class AudioProcessor : CPPObject() {
    private var filter: Filter? = null

    init {
        handler = create()
    }

    fun setSource(path: String) {
        if (0L == handler) return
        setSource(handler, path);
    }

    fun prepare() {
        if (0L == handler) return
        prepare(handler)
    }

    fun start() {
        if (0L == handler) return
        start(handler)
    }

    fun pause() {
        if (0L == handler) return
        pause(handler)
    }

    fun stop() {
        if (0L == handler) return
        stop(handler)
    }

    fun seek(us: Long) {
        if (0L == handler) return
        seek(handler, us)
    }

    fun release() {
        release(handler)
        handler = 0
    }

    private external fun create(): Long
    private external fun setSource(handler: Long, path: String)
    private external fun prepare(handler: Long)
    private external fun start(handler: Long)
    private external fun pause(handler: Long)
    private external fun stop(handler: Long)
    private external fun seek(handler: Long, us: Long)
    private external fun release(handler: Long)
}