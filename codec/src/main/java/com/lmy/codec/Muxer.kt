package com.lmy.codec

import com.lmy.codec.entity.Sample

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
interface Muxer {
    fun write(sample: Sample)
    fun release()
}