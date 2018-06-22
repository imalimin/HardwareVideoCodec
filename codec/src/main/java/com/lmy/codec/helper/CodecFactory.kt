/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.helper

import android.opengl.EGLContext
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.encoder.impl.SoftVideoEncoderImpl
import com.lmy.codec.encoder.impl.VideoEncoderImpl

/**
 * Created by lmyooyo@gmail.com on 2018/4/25.
 */
class CodecFactory {
    companion object {
        fun getEncoder(parameter: Parameter,
                       textureId: Int,
                       eglContext: EGLContext): Encoder {
            return if (Parameter.CodecType.HARD == parameter.codecType) {
                VideoEncoderImpl(parameter, textureId, eglContext)
            } else {
                SoftVideoEncoderImpl(parameter, textureId, eglContext)
            }
        }
    }
}