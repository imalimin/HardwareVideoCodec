/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.helper

import android.opengl.EGLContext
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.encoder.impl.SoftVideoEncoderV2Impl
import com.lmy.codec.encoder.impl.VideoEncoderImpl
import com.lmy.codec.entity.CodecContext

/**
 * Created by lmyooyo@gmail.com on 2018/4/25.
 */
class CodecFactory {
    companion object {
        fun getEncoder(context: CodecContext,
                       textureId: IntArray,
                       eglContext: EGLContext): Encoder {
            return if (CodecContext.CodecType.HARD == context.codecType) {
                VideoEncoderImpl(context, textureId, eglContext)
            } else {
                SoftVideoEncoderV2Impl(context, textureId, eglContext)
            }
        }
    }
}