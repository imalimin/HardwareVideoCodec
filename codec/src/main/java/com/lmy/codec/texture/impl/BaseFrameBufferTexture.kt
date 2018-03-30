package com.lmy.codec.texture.impl

import android.opengl.GLES20
import com.lmy.codec.util.debug_e
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/29.
 */
abstract class BaseFrameBufferTexture(var width: Int,
                                      var height: Int,
                                      var frameBuffer: Int? = null,
                                      var frameBufferTexture: Int? = null,
                                      var drawer: GLDrawer = GLDrawer()) : BaseTexture() {
    companion object {
        private val DRAW_INDICES = shortArrayOf(0, 1, 2, 0, 2, 3)
    }

    fun initFrameBuffer() {
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
        this.frameBuffer = frameBuffer[0]
        this.frameBufferTexture = frameBufferTex[0]
        debug_e("enable frame buffer: ${this.frameBuffer}, ${this.frameBufferTexture}")
    }

    class GLDrawer(var drawIndecesBuffer: ShortBuffer? = null) {
        init {
            drawIndecesBuffer = ByteBuffer.allocateDirect(2 * DRAW_INDICES.size).order(ByteOrder.nativeOrder()).asShortBuffer()
            drawIndecesBuffer?.put(DRAW_INDICES)
            drawIndecesBuffer?.position(0)
        }

        fun draw() {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawIndecesBuffer!!.limit(),
                    GLES20.GL_UNSIGNED_SHORT, drawIndecesBuffer)
        }
    }
}