package com.lmy.samplenative.processor

import android.graphics.SurfaceTexture

class PictureProcessor {
    private var handler: Long = 0

    init {
        handler = create()
    }

    fun prepare(surface: SurfaceTexture) {
        prepare(handler, surface)
    }

    private external fun create(): Long
    private external fun prepare(handler: Long, surface: SurfaceTexture)
}