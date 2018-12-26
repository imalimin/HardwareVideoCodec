package com.lmy.hwvc_native.processor

import android.view.Surface

class PictureProcessor {
    private var handler: Long = 0

    init {
        handler = create()
    }

    fun prepare(surface: Surface, width: Int, height: Int) {
        if (0L == handler) return
        prepare(handler, surface, width, height)
    }

    fun show(file: String) {
        if (0L == handler) return
        show(handler, file)
    }

    fun release() {
        release(handler)
        handler = 0
    }

    private external fun create(): Long
    private external fun prepare(handler: Long, surface: Surface, width: Int, height: Int)
    private external fun show(handler: Long, file: String)
    private external fun release(handler: Long)
}