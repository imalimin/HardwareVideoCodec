package com.lmy.codec.decoder

import android.graphics.SurfaceTexture

interface Decoder {
    var onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener?
    fun setInputResource(path: String)
    fun reset()
    fun prepare()
    fun start()
    fun pause()
    fun stop()
    fun getWidth(): Int
    fun getHeight(): Int
    fun getDuration(): Int
    fun release()
}