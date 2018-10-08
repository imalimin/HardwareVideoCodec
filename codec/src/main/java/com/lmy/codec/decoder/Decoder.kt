package com.lmy.codec.decoder

import android.media.MediaCodec
import java.nio.ByteBuffer

interface Decoder {
    val onSampleListener: OnSampleListener?
    fun reset()
    fun prepare()
    fun start()
    fun pause()
    fun stop()
    fun getDuration(): Int
    fun release()
    fun post(event: Runnable)
    interface OnSampleListener {
        fun onSample(decoder: Decoder, info: MediaCodec.BufferInfo, data: ByteBuffer)
    }
}