package com.lmy.codec.decoder

interface VideoDecoder : Decoder {
    companion object {
        const val KEY_ROTATION = "rotation-degrees"
    }

    fun getWidth(): Int
    fun getHeight(): Int
}