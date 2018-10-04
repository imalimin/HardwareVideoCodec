package com.lmy.codec.decoder

interface VideoDecoder:Decoder {
    fun getWidth(): Int
    fun getHeight(): Int
}