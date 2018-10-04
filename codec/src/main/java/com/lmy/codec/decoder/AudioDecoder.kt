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
}