package com.lmy.codec.decoder

import android.media.MediaCodec
import java.nio.ByteBuffer

interface AudioDecoder : Decoder {
    val onSampleListener: OnSampleListener?
    fun getSampleRateInHz(): Int
    fun getChannel(): Int
    interface OnSampleListener {
        fun onSample(decoder: Decoder, info: MediaCodec.BufferInfo, data: ByteBuffer)
    }

    companion object {
        val AAC_SAMPLING_FREQUENCIES = intArrayOf(
                96000, 88200, 64000, 48000,
                44100, 32000, 24000, 22050,
                16000, 12000, 11025, 8000,
                7350)
    }
}