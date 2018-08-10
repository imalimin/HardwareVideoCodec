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
import com.lmy.codec.helper.PixelsReader
import com.lmy.codec.helper.Resources
import com.lmy.codec.pipeline.EventPipeline
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.texture.impl.Rgb2YuvTexture
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
                           private var pTimer: VideoEncoderImpl.PresentationTimer = VideoEncoderImpl.PresentationTimer(context.video.fps),
                           override var onPreparedListener: Encoder.OnPreparedListener? = null,
                           override var onRecordListener: Encoder.OnRecordListener? = null)
    : Encoder, CacheX264Encoder.OnSampleListener {

    override fun onFormatChanged(format: MediaFormat) {
        onSampleListener?.onFormatChanged(this, format)
    }

    override fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer) {
        pTimer.record()
        info.presentationTimeUs = pTimer.presentationTimeUs
        onSampleListener?.onSample(this, info, data)
        onRecordListener?.onRecord(this, info.presentationTimeUs)
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
        mirrorTexture = Rgb2YuvTexture(context.video.width,
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
        onPreparedListener?.onPrepared(this)
        codec?.onSampleListener = this
    }

    private fun initPixelsCache() {
        reader = PixelsReader.create(Resources.instance.isSupportPBO(), context.video.width, context.video.height)
        reader?.start()
    }

    private fun encode() {
        synchronized(mEncodingSyn) {
            if (reader == null || !mEncoding) return
            save()
            codec?.encode(reader!!.getPixelsBuffer())
        }
    }

    var count = 0

    private fun save() {
        ++count
        if (150 == count) {
            debug_e("save yuv!")
            val buffer = reader!!.getPixelsBuffer()
            val ayuv = ByteArray(buffer.capacity())
            buffer.get(ayuv)
            val size = buffer.capacity() / 4
            val yuv = ByteArray(size * 3 / 2)
            var y0 = 0.toByte()
            var u0 = 0.toByte()
            var v0 = 0.toByte()

            var y1 = 0.toByte()
            var u1 = 0.toByte()
            var v1 = 0.toByte()

            var y2 = 0.toByte()
            var u2 = 0.toByte()
            var v2 = 0.toByte()

            var y3 = 0.toByte()
            var u3 = 0.toByte()
            var v3 = 0.toByte()
            for (i in 0 until context.video.height / 2) {
                for (j in 0 until context.video.width / 2) {
                    y0 = ayuv[i * 2 * context.video.width + j * 2 + 1]
                    u0 = ayuv[i * 2 * context.video.width + j * 2 + 2]
                    v0 = ayuv[i * 2 * context.video.width + j * 2 + 3]

                    y1 = ayuv[i * 2 * context.video.width + j * 2 + 5]
                    u1 = ayuv[i * 2 * context.video.width + j * 2 + 6]
                    v1 = ayuv[i * 2 * context.video.width + j * 2 + 7]

                    y2 = ayuv[(i * 2 + 1) * context.video.width + j * 2 + 1]
                    u2 = ayuv[(i * 2 + 1) * context.video.width + j * 2 + 2]
                    v2 = ayuv[(i * 2 + 1) * context.video.width + j * 2 + 3]

                    y3 = ayuv[(i * 2 + 1) * context.video.width + j * 2 + 5]
                    u3 = ayuv[(i * 2 + 1) * context.video.width + j * 2 + 6]
                    v3 = ayuv[(i * 2 + 1) * context.video.width + j * 2 + 7]
                }
            }
            for (i in 0 until ayuv.size / 16) {
                y0 = ayuv[i * 16 + 1]
                u0 = ayuv[i * 16 + 2]
                v0 = ayuv[i * 16 + 3]

                y1 = ayuv[i * 16 + 5]
                u1 = ayuv[i * 16 + 6]
                v1 = ayuv[i * 16 + 7]

                y2 = ayuv[i * 16 + 9]
                u2 = ayuv[i * 16 + 10]
                v2 = ayuv[i * 16 + 11]

                y3 = ayuv[i * 16 + 13]
                u3 = ayuv[i * 16 + 14]
                v3 = ayuv[i * 16 + 15]

                yuv[i * 4] = y0
                yuv[i * 4 + 1] = y1
                yuv[i * 4 + 2] = y2
                yuv[i * 4 + 3] = y3
                yuv[size]
            }
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