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
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CodecTextureWrapper
import com.lmy.codec.x264.CacheX264Encoder
import com.lmy.codec.x264.SurfaceX264Encoder
import java.nio.ByteBuffer

class SoftVideoEncoderV2Impl(var context: CodecContext,
                             private val textureId: IntArray,
                             private var eglContext: EGLContext,
                             override var onPreparedListener: Encoder.OnPreparedListener? = null,
                             var codecWrapper: CodecTextureWrapper? = null,
                             var codec: SurfaceX264Encoder? = null,
                             private var pTimer: PresentationTimer = PresentationTimer(context.video.fps),
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
    private val mEncodingSyn = Any()
    private var mEncoding = false
    private var inited = false

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        initCodec()
        codecWrapper = CodecTextureWrapper(codec!!.surface, textureId, eglContext)
        pTimer.reset()
        inited = true
    }

    private fun initCodec() {
        format = CodecHelper.createVideoFormat(context, true)!!
        codec = SurfaceX264Encoder(format, this)
                .post(Runnable {
                    codec!!.setProfile("high")
                    codec!!.setLevel(31)
                    codec?.start()
                    codec?.onSampleListener = this
                    onPreparedListener?.onPrepared(this)
                })
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
        codecWrapper?.release()
        codecWrapper = null
        debug_e("Video encoder stop")
    }

    override fun setPresentationTime(nsecs: Long) {

    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(mEncodingSyn) {
            if (mEncoding && inited) {
                codecWrapper?.egl?.makeCurrent()
                GLES20.glViewport(0, 0, context.video.width, context.video.height)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
                codecWrapper?.draw(null)
                codecWrapper?.egl?.swapBuffers()
            }
        }
    }
}