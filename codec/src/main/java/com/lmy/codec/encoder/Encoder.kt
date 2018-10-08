/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.encoder

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.EGLContext
import com.lmy.codec.encoder.impl.SoftVideoEncoderV2Impl
import com.lmy.codec.encoder.impl.VideoEncoderImpl
import com.lmy.codec.entity.CodecContext
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
interface Encoder : SurfaceTexture.OnFrameAvailableListener {
    var onPreparedListener: OnPreparedListener?
    var onRecordListener: OnRecordListener?
    fun start()
    fun pause()
    fun stop()
    fun setOnSampleListener(listener: OnSampleListener)
    fun setPresentationTime(nsecs: Long)

    interface OnSampleListener {
        fun onFormatChanged(encoder: Encoder, format: MediaFormat)
        fun onSample(encoder: Encoder, info: MediaCodec.BufferInfo, data: ByteBuffer)
    }

    interface OnStopListener {
        fun onStop()
    }

    interface OnPreparedListener {
        fun onPrepared(encoder: Encoder)
    }

    interface OnRecordListener {
        fun onRecord(encoder: Encoder, timeUs: Long)
    }

    class Builder(private val context: CodecContext,
                  private val textureId: IntArray,
                  private val eglContext: EGLContext,
                  private var onPreparedListener: OnPreparedListener? = null) {
        fun build(): Encoder {
            return if (CodecContext.CodecType.HARD == context.codecType) {
                VideoEncoderImpl(context, textureId, eglContext, onPreparedListener)
            } else {
                SoftVideoEncoderV2Impl(context, textureId, eglContext, onPreparedListener)
            }
        }

        fun setOnPreparedListener(listener: OnPreparedListener): Builder {
            onPreparedListener = listener
            return this
        }
    }
}