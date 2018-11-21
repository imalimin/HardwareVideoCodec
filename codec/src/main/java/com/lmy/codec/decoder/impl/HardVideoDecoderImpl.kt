/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.decoder.impl

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.view.Surface
import com.lmy.codec.decoder.Decoder
import com.lmy.codec.decoder.VideoDecoder
import com.lmy.codec.egl.entity.Egl
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Track
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import java.io.IOException
import kotlin.system.measureNanoTime


class HardVideoDecoderImpl(val context: CodecContext,
                           private val track: Track,
                           private val egl: Egl,
                           private val surfaceTexture: SurfaceTexture,
                           private val pipeline: Pipeline,
                           private val forPlay: Boolean = false,
                           override val onSampleListener: Decoder.OnSampleListener? = null) : VideoDecoder,
        SurfaceTexture.OnFrameAvailableListener {

    override var onStateListener: Decoder.OnStateListener? = null
    private var codec: MediaCodec? = null
    private var bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private var starting = false
    private var eos = false
    private var delay = 0L
    private var delayDelta = 0L

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {

    }

    override fun reset() {
        eos = false
        lastPts = 0
    }

    override fun delay(ns: Long) {
        delayDelta = ns
    }

    override fun prepare() {
        surfaceTexture.setOnFrameAvailableListener(this)
        pipeline?.queueEvent(Runnable {
            debug_i("VideoDecoder $track")
            debug_i("-----> Track selected")
            track.select()
            try {
                codec = MediaCodec.createDecoderByType(track.format.getString(MediaFormat.KEY_MIME))
                codec!!.configure(track.format, Surface(surfaceTexture), null, 0)
            } catch (e: IOException) {
                debug_e("Cannot open decoder")
                return@Runnable
            }
            codec?.start()
        })
    }

    private var pts = 0L
    private var lastPts = 0L
    private var cost = 0L
    @Synchronized
    private fun next() {
        pipeline.queueEvent(Runnable {
            cost += measureNanoTime {
                decode()
                pts = dequeue()
            } / 1000
            if (forPlay) {
                if (pts <= 0 && 0L == lastPts) {
                    cost = 0
                }
                if (pts > 0) {//成功解码了一帧
                    delay = (pts - lastPts - cost) / 1000
                    debug_i("$pts - $lastPts - $cost=$delay, delta=$delayDelta")
                    delayDelta = 0
                    lastPts = pts
                    pts = 0
                    cost = 0
                }
            } else {
                delay = 0
            }
            if (!eos) {
                next()
            } else {
                flush()
            }
        }, delay)
    }

    private fun decode(): Boolean {
        synchronized(this@HardVideoDecoderImpl) {
            if (!starting) return false
            egl.makeCurrent()
            val index = codec!!.dequeueInputBuffer(WAIT_TIME)
            if (index >= 0) {
                val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    codec!!.getInputBuffer(index)
                } else {
                    codec!!.inputBuffers[index]
                }
                synchronized(track.extractor) {
                    val size = track.readSampleData(buffer, 0)
                    if (size < 0) {//结束
                        codec!!.queueInputBuffer(index, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        debug_e("Track eos!")
                        eos = true
                        pause()
                    } else {//下一帧
                        eos = false
                        codec!!.queueInputBuffer(index, 0, size, track.getSampleTime(), 0)
                        track.advance()
                    }
                }
                return true
            }
            debug_e("Cannot get input buffer!")
            return false
        }
    }

    private fun dequeue(): Long {
        val index = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
        when (index) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> debug_i("INFO_OUTPUT_FORMAT_CHANGED")
            MediaCodec.INFO_TRY_AGAIN_LATER -> debug_i("INFO_TRY_AGAIN_LATER")
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> debug_i("INFO_OUTPUT_BUFFERS_CHANGED")
            else -> {
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    debug_e("dequeue stream end, flag=${bufferInfo.flags}")
                    onStateListener?.onEnd(this)
                } else {
                    onSampleListener?.onSample(this, bufferInfo, null)
                }
                codec!!.releaseOutputBuffer(index, true)
                return bufferInfo.presentationTimeUs
            }
        }
        return -1
    }

    override fun flush() {
        pipeline.queueEvent(Runnable {
            val index = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
            debug_i("stream index=$index, flag=${bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM}")
            when (index) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> debug_i("INFO_OUTPUT_FORMAT_CHANGED")
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    flush()
                    debug_i("INFO_TRY_AGAIN_LATER")
                }
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> debug_i("INFO_OUTPUT_BUFFERS_CHANGED")
                else -> {
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        debug_e("stream end")
                        onStateListener?.onEnd(this)
                    } else {
                        debug_i("stream flush")
                        onSampleListener?.onSample(this, bufferInfo, null)
                        flush()
                    }
                    codec!!.releaseOutputBuffer(index, true)
                }
            }
        })
    }

    override fun start() {
        if (eos) {
            debug_i("EOS!")
            return
        }
        if (starting) {
            debug_i("started")
            return
        }
        starting = true
        onStateListener?.onStart(this)
        next()
    }

    override fun pause() {
        if (!starting) {
            debug_i("paused")
            return
        }
        onStateListener?.onPause(this)
        starting = false
    }

    @Synchronized
    override fun stop() {
        pause()
        pipeline?.queueEvent(Runnable {
            codec?.stop()
            codec?.release()
            codec = null
        })
    }

    override fun release() {
        stop()
    }

    override fun post(event: Runnable) {
        pipeline.queueEvent(event)
    }

    override fun getWidth(): Int = context.video.width

    override fun getHeight(): Int = context.video.height

    override fun getDuration(): Int = if (null != track)
        track!!.format.getInteger(MediaFormat.KEY_DURATION) / 1000 else 0

    companion object {
        private const val WAIT_TIME = 10000L
    }
}