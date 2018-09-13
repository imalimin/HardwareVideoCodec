/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES30
import com.lmy.codec.entity.Egl
import com.lmy.codec.entity.PixelsBuffer
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/6/11.
 */

class PixelsReader private constructor(private var usePbo: Boolean,
                                       private var width: Int = 0,
                                       private var height: Int = 0,
                                       private var pbos: IntArray? = null,
                                       private var pixelsBuffer: PixelsBuffer? = null) {

    private var index = 0
    private var nextIndex = 1

    init {
        config(usePbo, width, height)
    }

    fun config(usePbo: Boolean, width: Int, height: Int) {
        this.width = width
        this.height = height
        this.usePbo = usePbo
    }

    private fun initPBOs() {
        if (!enablePBO()) {
            pixelsBuffer = PixelsBuffer.allocate(width * height * Egl.COLOR_CHANNELS)
            return
        }
        val size = width * height * Egl.COLOR_CHANNELS
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
        if (null != pixelsBuffer && null != pixelsBuffer!!.buffer && !pixelsBuffer!!.isInvalid)
            return
        if (!enablePBO()) {//不使用PBO
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
        GLES20.glReadPixels(0, 0, width, height, Egl.GL_CLOLR_DEFAULT,
                GLES20.GL_UNSIGNED_BYTE, pixelsBuffer!!.buffer)
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, GLES20.GL_NONE)
    }

    private fun readPixelsFromPBO(frameBuffer: Int) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer)
//        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
//                GLES30.GL_TEXTURE_2D, texture.frameBufferTexture!!, 0)
        //绑定到第一个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos!![index])
        GLHelper.glReadPixels(0, 0, width, height, Egl.GL_CLOLR_DEFAULT, GLES30.GL_UNSIGNED_BYTE)
        //绑定到第二个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos!![nextIndex])
        //glMapBufferRange会等待DMA传输完成，所以需要交替使用pbo
        //映射内存
        pixelsBuffer = PixelsBuffer.wrap(GLES30.glMapBufferRange(GLES30.GL_PIXEL_PACK_BUFFER,
                0, width * height * Egl.COLOR_CHANNELS, GLES30.GL_MAP_READ_BIT) as ByteBuffer)
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
            GLES20.glDeleteBuffers(pbos!!.size, pbos, 0)
        }
        if (pixelsBuffer != null) {
            pixelsBuffer!!.clear()
            pixelsBuffer = null
        }
    }

    fun enablePBO(): Boolean {
        return usePbo
    }

    fun currentIndex(): Int {
        return index
    }

    fun start() {
        if (width < 1 || height < 1) {
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

    private fun btoi(btarr: ByteArray): IntArray? {
        if (btarr.size % 4 != 0) {
            return null
        }
        val intarr = IntArray(btarr.size / 4)

        var i1: Int
        var i2: Int
        var i3: Int
        var i4: Int
        var j = 0
        var k = 0
        while (j < intarr.size)
        //j循环int        k循环byte数组
        {
            i1 = btarr[k].toInt()
            i2 = btarr[k + 1].toInt()
            i3 = btarr[k + 2].toInt()
            i4 = btarr[k + 3].toInt()

            if (i1 < 0) {
                i1 += 256
            }
            if (i2 < 0) {
                i2 += 256
            }
            if (i3 < 0) {
                i3 += 256
            }
            if (i4 < 0) {
                i4 += 256
            }
            intarr[j] = (i1 shl 24) + (i2 shl 16) + (i3 shl 8) + (i4 shl 0)//保存Int数据类型转换
            j++
            k += 4

        }
        return intarr
    }

    private val opts = BitmapFactory.Options()

    private fun convertARGB(data: ByteArray) {
        val width = width
        val height = height
        for (j in 0 until height) {
            for (i in 0 until width) {
                val index = width * 4 * j + i * 4
                val a = data[index + 3]
                data[index + 3] = data[index + 2]
                data[index + 2] = data[index + 1]
                data[index + 1] = data[index]
                data[index] = a
            }
        }
    }

    private fun save(data: ByteArray, path: String) {
//        val fos = FileOutputStream(path)
//        YuvImage(data, ImageFormat.YUY2, width, height, null)
//                .compressToJpeg(Rect(0, 0, width, height), 80, fos)
//        fos.close()
        convertARGB(data)
        val bitmap = Bitmap.createBitmap(btoi(data), width, height, Bitmap.Config.ARGB_8888)
        //        opts.inJustDecodeBounds = false;
        //        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //        Bitmap bitmap = BitmapFactory.decodeByteArray(frame.data, 0, frame.data.length, opts);
        try {
            val os = FileOutputStream(File(path))
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, os)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (null != bitmap && !bitmap.isRecycled)
            bitmap.recycle()
    }

    fun shoot(path: String) {
        val src = ByteArray(width * height * 3)
        getPixelsBuffer().get(src)
        save(src, path)
    }

    companion object {
        fun create(usePbo: Boolean, width: Int, height: Int): PixelsReader {
            return PixelsReader(usePbo, width, height)
        }

        private val PBO_COUNT = 2
    }
}
