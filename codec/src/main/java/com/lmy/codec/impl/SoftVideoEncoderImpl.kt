/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.impl

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.EGLContext
import android.opengl.GLES20
import android.opengl.GLES30
import com.lmy.codec.Encoder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.helper.GLHelper
import com.lmy.codec.pipeline.EventPipeline
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.texture.impl.MirrorTexture
import com.lmy.codec.util.debug_e
import com.lmy.codec.x264.CacheX264Encoder
import com.lmy.codec.x264.X264Encoder
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by lmyooyo@gmail.com on 2018/4/3.
 */
class SoftVideoEncoderImpl(var parameter: Parameter,
                           textureId: Int,
                           private var eglContext: EGLContext,
                           var codec: CacheX264Encoder? = null,
                           private var pbos: IntArray = IntArray(PBO_COUNT),
                           private var srcBuffer: ByteBuffer? = null,
                           private var pTimer: VideoEncoderImpl.PresentationTimer = VideoEncoderImpl.PresentationTimer(parameter.video.fps))
    : Encoder, CacheX264Encoder.OnSampleListener {
    override fun onFormatChanged(format: MediaFormat) {
        onSampleListener?.onFormatChanged(this, format)
    }

    override fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer) {
        pTimer.record()
        info.presentationTimeUs = pTimer.presentationTimeUs
        onSampleListener?.onSample(this, info, data)
    }

    companion object {
        val PBO_COUNT = 2
        val STOP = 0x3
    }

    private lateinit var format: MediaFormat
    private var mirrorTexture: BaseFrameBufferTexture
    private var mPipeline = EventPipeline.create("EncodePipeline")
    private val mEncodingSyn = Any()
    private var mEncoding = false
    //For PBO
    private var index = 0
    private var nextIndex = 1
    private var inited = false
    private var isSupportPbo = true

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        initCodec()
        initPixelsCache()
        mirrorTexture = MirrorTexture(parameter.video.width,
                parameter.video.height, textureId)
        mPipeline.queueEvent(Runnable {
            pTimer.reset()
            inited = true
        })
    }

    private fun initCodec() {
        format = CodecHelper.createVideoFormat(parameter, true)!!
        val c = X264Encoder(format)
//        c.setProfile("high")
//        codec?.setLevel(31)
        codec = CacheX264Encoder(parameter.video.width * parameter.video.height * 4, c)
        codec?.onSampleListener = this
    }

    private fun initPixelsCache() {
        val size = parameter.video.width * parameter.video.height * 4
        isSupportPbo = GLHelper.isSupportPBO(parameter.context)
        if (isSupportPbo) {
            initPBOs(size)
        } else {
            srcBuffer = ByteBuffer.allocate(size)
            srcBuffer?.order(ByteOrder.nativeOrder())
        }
    }

    private fun initPBOs(size: Int) {
        pbos = IntArray(PBO_COUNT)
        GLES30.glGenBuffers(PBO_COUNT, pbos, 0)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[0])
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_DYNAMIC_READ)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[1])
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_DYNAMIC_READ)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        debug_e("initPBOs(" + pbos[0] + ", " + pbos[1] + ")")
    }

    private fun encode() {
        synchronized(mEncodingSyn) {
            if (srcBuffer == null || !mEncoding) return
            codec?.encode(srcBuffer!!)
        }
    }

    private fun readPixels() {
        srcBuffer!!.clear()
        srcBuffer!!.position(0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mirrorTexture.frameBuffer!!)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, mirrorTexture.frameBufferTexture!!, 0)
        GLES20.glReadPixels(0, 0, parameter.video.width, parameter.video.height, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, srcBuffer!!)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
    }

    private fun readPixelsByPbo() {
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraWrapper.getFrameTexture())
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mirrorTexture.frameBuffer!!)
//        //用作纹理的颜色缓冲区，glReadPixels从这个颜色缓冲区中读取
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, mirrorTexture.frameBufferTexture!!, 0)
//        GLES30.glReadBuffer(GLES30.GL_FRONT)
        //绑定到第一个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[index])
        GLHelper.glReadPixels(0, 0, parameter.video.width, parameter.video.height,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE)
        //绑定到第二个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[nextIndex])
        //glMapBufferRange会等待DMA传输完成，所以需要交替使用pbo
        //映射内存
        srcBuffer = GLES30.glMapBufferRange(GLES30.GL_PIXEL_PACK_BUFFER,
                0, parameter.video.width * parameter.video.height * 4,
                GLES30.GL_MAP_READ_BIT) as ByteBuffer
        //解除映射
        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        //解除绑定PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, GLES20.GL_NONE)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES20.GL_NONE)
        //交换索引
        index = (index + 1) % 2
        nextIndex = (nextIndex + 1) % 2
        if (null == srcBuffer) {
            debug_e("PBO is null(${pbos[0]}, ${pbos[1]})")
            return
        }
//        debug_e("buffer[${srcBuffer!![2000]}, ${srcBuffer!![2001]}, ${srcBuffer!![2002]}, ${srcBuffer!![2003]}]")
    }

    private fun readPixels(pbo: Boolean) {
        GLES20.glViewport(0, 0, parameter.video.width, parameter.video.height)
        mirrorTexture.drawTexture(null)
        if (pbo) readPixelsByPbo()
        else readPixels()
    }


    @Throws(FileNotFoundException::class)
    private fun shotScreen(data: ByteArray, width: Int, height: Int) {
        val dataTmp = IntArray(data.size / 4)
        for (i in dataTmp.indices) {
            dataTmp[i] = Color.argb(data[i * 4 + 3].toInt(), data[i * 4].toInt(),
                    data[i * 4 + 1].toInt(), data[i * 4 + 2].toInt())
        }
        val bitmap = Bitmap.createBitmap(dataTmp, width, height, Bitmap.Config.ARGB_8888)
        if (null == bitmap) {
            debug_e("Bitmap is null")
            return
        }
        val out = FileOutputStream("/storage/emulated/0/000.jpg")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        debug_e("Saved!")
    }

    override fun start() {
        synchronized(mEncodingSyn) {
            pTimer.start()
            mEncoding = true
        }
    }

    override fun pause() {
        synchronized(mEncodingSyn) {
            mEncoding = false
        }
    }

    override fun stop() {
        stop(null)
    }

    override fun stop(listener: Encoder.OnStopListener?) {
        pause()
        mPipeline.queueEvent(Runnable {
            codec?.release()
            listener?.onStop()
        })
        mPipeline.quit()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(mEncodingSyn) {
            if (mEncoding && inited) {
                readPixels(isSupportPbo)
                mPipeline.queueEvent(Runnable { encode() })
            }
        }
    }
}