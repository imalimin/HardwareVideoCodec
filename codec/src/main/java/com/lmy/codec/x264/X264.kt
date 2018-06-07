package com.lmy.codec.x264

import android.media.MediaCodec

/**
 * Created by lmyooyo@gmail.com on 2018/6/7.
 */
interface X264 {
    fun encode(src: ByteArray, srcSize: Int): MediaCodec.BufferInfo?
    fun getWidth(): Int
    fun getHeight(): Int
    fun stop()
}