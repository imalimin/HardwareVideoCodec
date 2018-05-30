package com.lmy.codec.texture.impl.filter

import android.opengl.GLES20
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.util.debug_e

/**
 * Created by lmyooyo@gmail.com on 2018/4/25.
 */
abstract class BaseFilter(width: Int = 0,
                          height: Int = 0,
                          textureId: Int = -1) : BaseFrameBufferTexture(width, height, textureId) {
    abstract fun init()
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

    companion object {
        private var shareFrameBuffer: Int? = null
        private var shareFrameBufferTexture: Int? = null
    }
}