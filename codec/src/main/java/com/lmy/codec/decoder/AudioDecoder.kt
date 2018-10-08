package com.lmy.codec.decoder

interface AudioDecoder : Decoder {
    fun getSampleRate(): Int
    fun getChannel(): Int

    companion object {
        val AAC_SAMPLING_FREQUENCIES = intArrayOf(
                96000, 88200, 64000, 48000,
                44100, 32000, 24000, 22050,
                16000, 12000, 11025, 8000,
                7350)
    }
}