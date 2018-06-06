package com.lmy.codec.texture.impl.filter

import android.opengl.GLES20
import com.lmy.codec.BaseApplication
import com.lmy.codec.helper.AssetsHelper
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.util.debug_e
import java.nio.FloatBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/4/25.
 */
abstract class BaseFilter(width: Int = 0,
                          height: Int = 0,
                          textureId: Int = -1) : BaseFrameBufferTexture(width, height, textureId) {
    open fun init() {
        verticesBuffer = createShapeVerticesBuffer(getVerticesBuffer())
        shaderProgram = createProgram(AssetsHelper.read(BaseApplication.assetManager(), getVertex()),
                AssetsHelper.read(BaseApplication.assetManager(), getFragment()))
        initFrameBuffer()
    }

    override fun initFrameBuffer() {
        if (null != shareFrameBuffer && null != shareFrameBufferTexture) {
            this.frameBuffer = shareFrameBuffer
            this.frameBufferTexture = shareFrameBufferTexture
            debug_e("enable share frame buffer: ${this.frameBuffer}, ${this.frameBufferTexture}")
            return
        }
        val frameBuffer = IntArray(1)
        val frameBufferTex = IntArray(1)
        GLES20.glGenFramebuffers(1, frameBuffer, 0)
        GLES20.glGenTextures(1, frameBufferTex, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTex[0])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, frameBufferTex[0], 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = "initFrameBuffer: glError 0x" + Integer.toHexString(error)
            debug_e(msg)
            return
        }
        shareFrameBuffer = frameBuffer[0]
        shareFrameBufferTexture = frameBufferTex[0]
        this.frameBuffer = shareFrameBuffer
        this.frameBufferTexture = shareFrameBufferTexture
        debug_e("enable frame buffer: ${this.frameBuffer}, ${this.frameBufferTexture}")
    }

    fun active() {
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
    }

    fun uniform1i(uniform: Int, x: Int) {
        setUniform1i(uniform, x)
    }

    fun enableVertex(position: Int, coordinate: Int) {
        enableVertex(position, coordinate, buffer!!, verticesBuffer!!)
    }

    fun draw() {
        drawer.draw()
        GLES20.glFinish()
    }

    fun disableVertex(position: Int, coordinate: Int) {
        GLES20.glDisableVertexAttribArray(position)
        GLES20.glDisableVertexAttribArray(coordinate)
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

    companion object {
        private var shareFrameBuffer: Int? = null
        private var shareFrameBufferTexture: Int? = null
        private val VERTICES = floatArrayOf(
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f)
    }
}