package com.lmy.codec.x264

import android.media.MediaCodec

/**
 * Created by lmyooyo@gmail.com on 2018/6/7.
 */
interface X264 {
    fun start()
    fun encode(src: ByteArray): MediaCodec.BufferInfo?
    fun getWidth(): Int
    fun getHeight(): Int
    fun setProfile(profile: String)
    fun setLevel(level: Int)
    fun stop()
    fun release()
    fun post(event: Runnable): X264
}