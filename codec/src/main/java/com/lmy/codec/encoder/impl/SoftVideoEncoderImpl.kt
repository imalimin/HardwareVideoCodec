/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.encoder.impl

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.EGLContext
import android.opengl.GLES20
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.helper.GLHelper
import com.lmy.codec.helper.PixelsReader
import com.lmy.codec.pipeline.EventPipeline
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.texture.impl.MirrorTexture
import com.lmy.codec.util.debug_e
import com.lmy.codec.x264.CacheX264Encoder
import com.lmy.codec.x264.X264Encoder
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/4/3.
 */
class SoftVideoEncoderImpl(var context: CodecContext,
                           textureId: IntArray,
                           private var eglContext: EGLContext,
                           var codec: CacheX264Encoder? = null,
                           var reader: PixelsReader? = null,
                           private var pTimer: VideoEncoderImpl.PresentationTimer = VideoEncoderImpl.PresentationTimer(context.video.fps))
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
    private var mPipeline = EventPipeline.create("VideoEncodePipeline")
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
        mirrorTexture = MirrorTexture(context.video.width,
                context.video.height, textureId)
        mPipeline.queueEvent(Runnable {
            pTimer.reset()
            inited = true
        })
    }

    private fun initCodec() {
        format = CodecHelper.createVideoFormat(context, true)!!
        val c = X264Encoder(format)
//        c.setProfile("high")
//        codec?.setLevel(31)
        codec = CacheX264Encoder(context.video.width * context.video.height * 4, c)
        codec?.onSampleListener = this
    }

    private fun initPixelsCache() {
        isSupportPbo = GLHelper.isSupportPBO(context.context)
        reader = PixelsReader.create(context.context, context.video.width, context.video.height)
        reader?.start()
    }

    private fun encode() {
        synchronized(mEncodingSyn) {
            if (reader == null || !mEncoding) return
            codec?.encode(reader!!.getPixelsBuffer())
        }
    }

    private fun readPixels() {
        GLES20.glViewport(0, 0, context.video.width, context.video.height)
        mirrorTexture.drawTexture(null)
        reader?.readPixels(mirrorTexture.frameBuffer[0])
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
        pause()
        debug_e("Video encoder stopping")
        codec?.release()
        reader?.stop()
        mPipeline.quit()
        debug_e("Video encoder stop")
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(mEncodingSyn) {
            if (mEncoding && inited) {
                readPixels()
                mPipeline.queueEvent(Runnable { encode() })
                reader?.recycleBuffer()
            }
        }
    }
}