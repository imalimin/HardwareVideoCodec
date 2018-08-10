/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.encoder.impl

import android.graphics.*
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.EGLContext
import android.opengl.GLES20
import android.os.Environment
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.helper.PixelsReader
import com.lmy.codec.helper.Resources
import com.lmy.codec.pipeline.EventPipeline
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.texture.impl.MirrorTexture
import com.lmy.codec.util.debug_e
import com.lmy.codec.x264.CacheX264Encoder
import com.lmy.codec.x264.X264Encoder
import java.io.File
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

    private lateinit var format: MediaFormat
    private var mirrorTexture: BaseFrameBufferTexture
    private var mPipeline = EventPipeline.create("VideoEncodePipeline")
    private val mEncodingSyn = Any()
    private var mEncoding = false
    private var inited = false

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        initPixelsCache()
        mirrorTexture = MirrorTexture(context.video.width,
                context.video.height, textureId)
        mPipeline.queueEvent(Runnable {
            initCodec()
            pTimer.reset()
            inited = true
        })
    }

    private fun initCodec() {
        format = CodecHelper.createVideoFormat(context, true)!!
        val c = X264Encoder(format)
        c.setProfile("high")
        c.setLevel(31)
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
//            save()
            codec?.encode(reader!!.getPixelsBuffer())
        }
    }

    var count = 0

    private fun save() {
        ++count
        if (150 == count) {
            val time = System.currentTimeMillis()
            val buffer = reader!!.getPixelsBuffer()
            val ayuv = ByteArray(buffer.capacity())
            buffer.get(ayuv)
            val size = buffer.capacity() / 4
            val yuv = ByteArray(size * 3 / 2)
            var y0: Byte
            var u0: Byte
            var v0: Byte

            var y1: Byte
            var u1: Byte
            var v1: Byte

            var y2: Byte
            var u2: Byte
            var v2: Byte

            var y3: Byte
            var u3: Byte
            var v3: Byte
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

                    yuv[i * 2 * context.video.width + j * 2] = y0
                    yuv[i * 2 * context.video.width + j * 2 + 1] = y1
                    yuv[(i * 2 + 1) * context.video.width + j * 2] = y2
                    yuv[(i * 2 + 1) * context.video.width + j * 2 + 1] = y3

                    yuv[size + i * context.video.width + j * 2] = ((v0 + v1 + v2 + v3) / 4).toByte()
                    yuv[size + i * context.video.width + j * 2 + 1] = ((u0 + u1 + u2 + u3) / 4).toByte()
                }
            }
            debug_e("save yuv: ${System.currentTimeMillis() - time}")
            val image = YuvImage(yuv, ImageFormat.NV21,
                    context.video.width, context.video.height, null)
            val fos = FileOutputStream(File(Environment.getExternalStorageDirectory(), "yuv.jpg"))
            image.compressToJpeg(Rect(0, 0, context.video.width, context.video.height), 80, fos)
            fos.close()
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