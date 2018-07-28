package com.lmy.codec.native.processor

class DeNoise(sampleRate: Int, sampleSize: Int) {
    init {
        System.loadLibrary("speexdsp")
        start(sampleRate, sampleSize)
    }

    private external fun start(sampleRate: Int, sampleSize: Int)
    external fun preprocess(data: ByteArray): Int
    external fun stop()
}