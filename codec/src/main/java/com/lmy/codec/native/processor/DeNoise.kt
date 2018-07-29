package com.lmy.codec.native.processor

import com.lmy.codec.util.debug_i

class DeNoise(sampleRate: Int, sampleSize: Int) {
    init {
        System.loadLibrary("speexdsp")
        debug_i("Setup DeNoise, sampleRate=$sampleRate, sampleSize=$sampleSize")
        start(sampleRate, sampleSize)
    }

    private external fun start(sampleRate: Int, sampleSize: Int)
    external fun preprocess(data: ByteArray): Int
    external fun stop()
}