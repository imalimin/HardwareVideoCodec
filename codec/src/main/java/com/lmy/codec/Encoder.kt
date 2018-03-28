package com.lmy.codec

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
interface Encoder : SurfaceTexture.OnFrameAvailableListener {
    fun start()
    fun pause()
    fun stop()
    fun stop(listener: OnStopListener?)
    fun setOnSampleListener(listener: OnSampleListener)

    interface OnSampleListener {
        fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer)
    }

    interface OnStopListener {
        fun onStop()
    }
}