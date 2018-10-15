/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.encoder.impl

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.EGLContext
import android.opengl.GLES20
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.PresentationTimer
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.helper.PixelsReader
import com.lmy.codec.helper.Resources
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.texture.impl.MirrorTexture
import com.lmy.codec.util.debug_e
import com.lmy.codec.x264.CacheX264Encoder
import com.lmy.codec.x264.X264Encoder
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/4/3.
 */
class SoftVideoEncoderImpl(var context: CodecContext,
                           textureId: IntArray,
                           private var eglContext: EGLContext,
                           override var onPreparedListener: Encoder.OnPreparedListener? = null,
                           var codec: CacheX264Encoder? = null,
                           var reader: PixelsReader? = null,
                           private var pTimer: PresentationTimer = PresentationTimer(context.video.fps),
                           override var onRecordListener: Encoder.OnRecordListener? = null)
    : Encoder, CacheX264Encoder.OnSampleListener {
    override fun getOutputFormat(): MediaFormat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFormatChanged(format: MediaFormat) {
        onSampleListener?.onFormatChanged(this, format)
    }

    override fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer) {
        pTimer.record()
        info.presentationTimeUs = pTimer.presentationTimeUs / 1000
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
        codec = CacheX264Encoder(X264Encoder(format))
        codec!!.setProfile("high")
        codec!!.setLevel(31)
        codec?.start()
        codec?.onSampleListener = this
        onPreparedListener?.onPrepared(this)
    }

    private fun initPixelsCache() {
        reader = PixelsReader.create(Resources.instance.isSupportPBO(), context.video.width, context.video.height)
        reader?.start()
    }

    override fun setPresentationTime(nsecs: Long) {

    }

    private fun encode() {
        synchronized(mEncodingSyn) {
            if (reader == null || !mEncoding) return
//            ++count
//            if (0 == count % 30)
//                reader?.shoot("/sdcard/ttt.jpg")
            codec?.encode(reader!!.getPixelsBuffer())
        }
    }

    var count = 0

    private fun readPixels() {
        GLES20.glViewport(0, 0, context.video.width, context.video.height)
        mirrorTexture.draw(null)
        reader?.readPixels(mirrorTexture.frameBuffer[0])
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