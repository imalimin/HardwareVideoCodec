package com.lmy.samplenative.processor

import android.view.Surface

class PictureProcessor {
    private var handler: Long = 0

    init {
        handler = create()
    }

    fun prepare(surface: Surface) {
        prepare(handler, surface)
    }

    private external fun create(): Long
    private external fun prepare(handler: Long, surface: Surface)
}