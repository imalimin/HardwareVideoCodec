package com.lmy.codec.entity

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class Sample private constructor(var bufferInfo: MediaCodec.BufferInfo,
                                 var sample: ByteBuffer) {
    companion object {
        fun wrap(bufferInfo: MediaCodec.BufferInfo, sample: ByteBuffer): Sample {
            return Sample(bufferInfo, sample)
        }
    }
}