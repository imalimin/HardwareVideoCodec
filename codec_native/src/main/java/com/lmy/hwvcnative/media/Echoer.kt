package com.lmy.hwvcnative.media

import com.lmy.hwvcnative.CPPObject

class Echoer : CPPObject() {
    init {
        handler = create(2, 44100, 0x0010, 1024)
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