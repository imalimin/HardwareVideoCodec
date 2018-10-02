package com.lmy.codec.decoder

import android.graphics.SurfaceTexture
import com.lmy.codec.wrapper.CameraTextureWrapper

interface Decoder {
    var onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener?
    var textureWrapper: CameraTextureWrapper?
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
    fun post(event: Runnable)
}