package com.lmy.codec.decoder

import android.media.MediaCodec
import java.nio.ByteBuffer

interface Decoder {
    val onSampleListener: OnSampleListener?
    var onStateListener: OnStateListener?
    fun reset()
    fun prepare()
    fun start()
    fun pause()
    fun stop()
    fun getDuration(): Int
    fun release()
    fun post(event: Runnable)
    interface OnSampleListener {
        fun onSample(decoder: Decoder, info: MediaCodec.BufferInfo, data: ByteBuffer?)
    }

    interface OnStateListener {
        fun onStart(decoder: Decoder)
        fun onPause(decoder: Decoder)
        fun onEnd(decoder: Decoder)
    }
}