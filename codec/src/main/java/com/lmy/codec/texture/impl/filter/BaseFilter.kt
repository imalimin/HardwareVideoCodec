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
import com.lmy.codec.util.debug_e
import java.nio.FloatBuffer

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

    override fun initFrameBuffer() {
        if (null != shareFrameBuffer && null != shareFrameBufferTexture) {
            this.frameBuffer = shareFrameBuffer!!
            this.frameBufferTexture = shareFrameBufferTexture!!
            debug_e("enable share frame buffer: ${this.frameBuffer[0]}, ${this.frameBufferTexture[0]}")
            return
        }
        super.initFrameBuffer()
        shareFrameBuffer = this.frameBuffer
        shareFrameBufferTexture = this.frameBufferTexture
    }

    fun active() {
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
    }

    fun uniform1i(uniform: Int, x: Int) {
        setUniform1i(uniform, x)
    }

    fun draw() {
        drawer.draw()
        GLES20.glFinish()
    }

    fun inactive() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
    }


    fun getVerticesBuffer(): FloatArray {
        return VERTICES
    }

    open fun setValue(index: Int, value: Int) {

    }

    abstract fun getVertex(): String
    abstract fun getFragment(): String

    fun setUniform1i(location: Int, value: Int) {
        GLES20.glUniform1i(location, value)
    }

    fun setUniform1f(location: Int, floatValue: Float) {
        GLES20.glUniform1f(location, floatValue)
    }

    fun setUniform2fv(location: Int, arrayValue: FloatArray) {
        GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    fun setUniform3fv(location: Int, arrayValue: FloatArray) {
        GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    fun setUniform4fv(location: Int, arrayValue: FloatArray) {
        GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    fun setUniformMatrix4fv(location: Int, arrayValue: FloatArray) {
        GLES20.glUniformMatrix4fv(location, 1, false, FloatBuffer.wrap(arrayValue))
    }

    override fun release() {
        if (null != shaderProgram)
            GLES20.glDeleteProgram(shaderProgram!!)
    }

    companion object {
        private var shareFrameBuffer: IntArray? = null
        private var shareFrameBufferTexture: IntArray? = null
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
            if (null != shareFrameBuffer)
                GLES20.glDeleteFramebuffers(1, shareFrameBuffer, 0)
            if (null != shareFrameBufferTexture)
                GLES20.glDeleteTextures(1, shareFrameBufferTexture, 0)
            shareFrameBuffer = null
            shareFrameBufferTexture = null
        }
    }
}