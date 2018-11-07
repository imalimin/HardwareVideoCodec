package com.lmy.codec.presenter

interface VideoProcessor : Processor {
    fun save(path: String, startMs: Int, endMs: Int, end: Runnable?)
    fun setBitrate(bitrate: Int)
}