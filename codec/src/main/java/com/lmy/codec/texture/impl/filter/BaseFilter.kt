/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import android.opengl.GLES20
import com.lmy.codec.helper.Resources
import com.lmy.codec.texture.impl.BaseFrameBufferTexture

/**
 * Created by lmyooyo@gmail.com on 2018/4/25.
 */
abstract class BaseFilter(width: Int = 0,
                          height: Int = 0,
                          textureId: IntArray) : BaseFrameBufferTexture(width, height, textureId) {
    open fun init() {
        name = "BaseFilter"
        shaderProgram = createProgram(Resources.instance.readAssetsAsString(getVertex()),
                Resources.instance.readAssetsAsString(getFragment()))
        initFrameBuffer()
    }

    open fun active(samplerLocation: Int) {
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
        setUniform1i(samplerLocation, 0)
    }

    protected fun draw() {
        drawer.draw()
    }

    fun inactive() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)
        GLES20.glFlush()
    }


    fun getVerticesBuffer(): FloatArray {
        return VERTICES
    }

    open fun setValue(index: Int, value: Int) {

    }

    abstract fun getVertex(): String
    abstract fun getFragment(): String

    companion object {
        //        private var shareFrameBuffer: IntArray? = null
//        private var shareFrameBufferTexture: IntArray? = null
        private val VERTICES = floatArrayOf(
                0.0f, 0.0f,//LEFT,BOTTOM
                1.0f, 0.0f,//RIGHT,BOTTOM
                0.0f, 1.0f,//LEFT,TOP
                1.0f, 1.0f//RIGHT,TOP
        )

        /**
         * This will release the shared resources,
         * please make sure to release at the last moment
         */
        fun release() {
//            if (null != shareFrameBuffer)
//                GLES20.glDeleteFramebuffers(1, shareFrameBuffer, 0)
//            if (null != shareFrameBufferTexture)
//                GLES20.glDeleteTextures(1, shareFrameBufferTexture, 0)
//            shareFrameBuffer = null
//            shareFrameBufferTexture = null
        }
    }
}