/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
interface Encoder : SurfaceTexture.OnFrameAvailableListener {
    fun start()
    fun pause()
    fun stop()
    fun stop(listener: OnStopListener?)
    fun setOnSampleListener(listener: OnSampleListener)

    interface OnSampleListener {
        fun onFormatChanged(format: MediaFormat)
        fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer)
    }

    interface OnStopListener {
        fun onStop()
    }
}