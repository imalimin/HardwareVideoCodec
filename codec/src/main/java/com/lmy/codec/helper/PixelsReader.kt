package com.lmy.codec.helper

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES30
import com.lmy.codec.entity.PixelsBuffer
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/6/11.
 */

class PixelsReader private constructor(context: Context, width: Int, height: Int,
                                       private var pbos: IntArray? = null,
                                       private var pixelsBuffer: PixelsBuffer? = null) {

    private var width: Int = 0
    private var height: Int = 0
    private var supportPBO = false
    private var enablePBO = false

    private var index = 0
    private var nextIndex = 1

    init {
        config(context, width, height)
    }

    fun config(context: Context, width: Int, height: Int) {
        this.width = width
        this.height = height
        supportPBO = GLHelper.isSupportPBO(context)
        pixelsBuffer = PixelsBuffer.allocate(width * height * 4)
    }

    private fun initPBOs() {
        if (!supportPBO) return
        val size = width * height * 4
        pbos = IntArray(PBO_COUNT)
        GLES30.glGenBuffers(PBO_COUNT, pbos, 0)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos!![0])
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_STATIC_READ)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos!![1])
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_STATIC_READ)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
    }

    fun readPixels(frameBuffer: Int) {
        //如果pixelsBuffer有效，则跳过
        if (null != pixelsBuffer && null != pixelsBuffer!!.getBuffer() && !pixelsBuffer!!.isInvalid())
            return
        if (!enablePBO) {//不使用PBO
            readPixelsFromFBO(frameBuffer)
        } else {
            readPixelsFromPBO(frameBuffer)
        }

        if (null == pixelsBuffer || null == pixelsBuffer!!.buffer) {
            debug_e("pixelsBuffer is null(" + pbos!![0] + ", " + pbos!![1] + ")")
            return
        }
        pixelsBuffer!!.position(0)
        //读完后标记为有效，避免重复读取
        if (null != pixelsBuffer) {
            pixelsBuffer!!.valid()
        }
    }

    private fun readPixelsFromFBO(frameBuffer: Int) {
        if (pixelsBuffer == null || null == pixelsBuffer!!.buffer) return
        pixelsBuffer!!.clear()
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer)
//        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
//                GLES30.GL_TEXTURE_2D, texture.frameBufferTexture!!, 0)
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, pixelsBuffer!!.buffer)
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, GLES20.GL_NONE)
    }

    private fun readPixelsFromPBO(frameBuffer: Int) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer)
//        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
//                GLES30.GL_TEXTURE_2D, texture.frameBufferTexture!!, 0)
        //绑定到第一个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos!![index])
        GLHelper.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE)
        //绑定到第二个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos!![nextIndex])
        //glMapBufferRange会等待DMA传输完成，所以需要交替使用pbo
        //映射内存
        pixelsBuffer = PixelsBuffer.wrap(GLES30.glMapBufferRange(GLES30.GL_PIXEL_PACK_BUFFER,
                0, width * height * 4, GLES30.GL_MAP_READ_BIT) as ByteBuffer)
        //            PushLog.e("glMapBufferRange: " + GLES30.glGetError());
        //解除映射
        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        //解除绑定PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, GLES20.GL_NONE)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES20.GL_NONE)
        //交换索引
        index = (index + 1) % PBO_COUNT
        nextIndex = (nextIndex + 1) % PBO_COUNT
    }

    private fun releasePBO() {
        if (null == pbos) return
        debug_i("releasePBO")
        if (null != pbos) {
            GLES30.glUnmapBuffer(pbos!![0])
            GLES30.glUnmapBuffer(pbos!![1])
        }
        if (pixelsBuffer != null) {
            pixelsBuffer!!.clear()
        }
        GLES20.glDeleteBuffers(pbos!!.size, pbos, 0)
    }

    fun enablePBO(): Boolean {
        return enablePBO
    }

    fun currentIndex(): Int {
        return index
    }

    fun start() {
        if (width < 1 || height < 1 || null == pixelsBuffer) {
            throw RuntimeException("You must config before start!")
        }
        initPBOs()
    }

    fun stop() {
        releasePBO()
    }

    fun recycleBuffer() {
        //用完后标记为无效状态
        if (null != pixelsBuffer && !pixelsBuffer!!.isInvalid()) {
            pixelsBuffer!!.invalid()
        }
    }

    fun getPixelsBuffer(): ByteBuffer {
        return pixelsBuffer!!.buffer
    }

    companion object {
        fun create(context: Context, width: Int, height: Int): PixelsReader {
            return PixelsReader(context, width, height)
        }

        private val PBO_COUNT = 2
    }
}
