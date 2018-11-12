/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl

import android.opengl.GLES20
import com.lmy.codec.egl.entity.Egl
import com.lmy.codec.helper.GLHelper
import com.lmy.codec.util.debug_e

/**
 * Created by lmyooyo@gmail.com on 2018/3/29.
 */
abstract class BaseFrameBufferTexture(var width: Int,
                                      var height: Int,
                                      textureId: IntArray,
                                      var frameBuffer: IntArray = IntArray(1),
                                      var frameBufferTexture: IntArray = IntArray(1)) : BaseTexture(textureId) {

    init {
        name = "BaseFrameBufferTexture"
    }

    open fun updateFrameBuffer(width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTexture[0])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, Egl.GL_COLOR_DEFAULT, this.width, this.height,
                0, Egl.GL_COLOR_DEFAULT, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
    }

    open fun initFrameBuffer() {
        releaseFrameBuffer()
        this.width = width
        this.height = height
        GLES20.glGenFramebuffers(1, frameBuffer, 0)
        GLES20.glGenTextures(1, frameBufferTexture, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTexture[0])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, Egl.GL_COLOR_DEFAULT, width, height, 0,
                Egl.GL_COLOR_DEFAULT, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                frameBufferTexture[0], 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        if (GLES20.GL_NO_ERROR != GLHelper.checkGLES2Error("$name initFrameBuffer")) {
            return
        }
        debug_e("$name enable frame buffer: ${this.frameBuffer[0]}, ${this.frameBufferTexture[0]}")
    }

    private fun releaseFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, frameBuffer, 0)
        GLES20.glDeleteTextures(1, frameBufferTexture, 0)
    }

    override fun release() {
        super.release()
        releaseFrameBuffer()
    }
}