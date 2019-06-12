package com.lmy.hwvcnative.media

import android.content.Context
import android.media.AudioManager
import com.lmy.hwvcnative.CPPObject

class HwEchoPlayer(context: Context) : CPPObject() {
    init {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sampleRateStr: String? = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        val sampleRate: Int = sampleRateStr?.let { str ->
            Integer.parseInt(str).takeUnless { it == 0 }
        } ?: 44100
        val samplesPerBufferStr: String? = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
        val samplesPerBuffer: Int = samplesPerBufferStr?.let { str ->
            Integer.parseInt(str).takeUnless { it == 0 }
        } ?: 256
        handler = create(2, sampleRate, 0x0010, samplesPerBuffer)
    }

    fun start() {
        start(handler)
    }

    fun stop() {
        stop(handler)
    }

    private external fun create(channels: Int, sampleHz: Int, format: Int, minBufferSize: Int): Long
    private external fun start(handler: Long): Long
    private external fun stop(handler: Long): Long
}