package com.lmy.codec.decoder

interface Decoder {
    fun reset()
    fun prepare()
    fun start()
    fun pause()
    fun stop()
    fun getDuration(): Int
    fun release()
    fun post(event: Runnable)
}