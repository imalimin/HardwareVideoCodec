/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.decoder.impl

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import com.lmy.codec.decoder.AudioDecoder
import com.lmy.codec.decoder.Decoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Track
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import java.io.IOException

class AudioDecoderImpl(val context: CodecContext,
                       private val track: Track,
                       private val forPlay: Boolean = false,
                       override val onSampleListener: Decoder.OnSampleListener? = null) : AudioDecoder {

    private val lock = Object()
    override var onStateListener: Decoder.OnStateListener? = null
    private var pipeline: Pipeline? = EventPipeline.create("AudioDecoderPipeline")
    private var mDequeuePipeline: Pipeline? = EventPipeline.create("AudioDequeuePipeline")
    private var codec: MediaCodec? = null
    private var bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private var sampleSize: Int = 0
    private var starting = false
    private var codecStarted = false
    private var eos = false
    private var lastPts = 0L

    override fun reset() {
        eos = false
        lastPts = 0
    }

    override fun prepare() {
        pipeline?.queueEvent(Runnable {
            debug_i("AudioDecoder $track")
            debug_i("AudioDecoder channel=${getChannel()}")
            debug_i("-----> Track selected")
            track.select()
            try {
                codec = MediaCodec.createDecoderByType(track.format.getString(MediaFormat.KEY_MIME))
                codec!!.configure(track.format, null, null, 0)
            } catch (e: IOException) {
                debug_e("Cannot open decoder")
                return@Runnable
            }
            codec?.start()
            codecStarted = true
            synchronized(lock) {
                lock.notifyAll()
            }
        })
    }

    private fun dequeue() {
        val index = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
        when (index) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
//                    debug_i("INFO_OUTPUT_FORMAT_CHANGED")
                return
            }
            MediaCodec.INFO_TRY_AGAIN_LATER -> {
//                    debug_i("INFO_TRY_AGAIN_LATER")
                return
            }
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
//                    debug_i("INFO_OUTPUT_BUFFERS_CHANGED")
                return
            }
            else -> {
                if (index >= 0) {
                    if (sampleSize <= 0) {
                        sampleSize = bufferInfo.size
                    }
                    val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        codec!!.getOutputBuffer(index)
                    } else {
                        codec!!.outputBuffers[index]
                    }
                    onSampleListener?.onSample(this@AudioDecoderImpl, bufferInfo, buffer)
                    buffer.clear()
                    codec!!.releaseOutputBuffer(index, false)
                } else {
                    return
                }
            }
        }
    }

    override fun flush() {
        pipeline?.queueEvent(Runnable {
            val index = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
            when (index) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                }
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    flush()
                }
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                }
                else -> {
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        debug_e("stream end")
                        onStateListener?.onEnd(this)
                    } else {
                        debug_i("stream flush")
                        val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            codec!!.getOutputBuffer(index)
                        } else {
                            codec!!.outputBuffers[index]
                        }
                        onSampleListener?.onSample(this@AudioDecoderImpl, bufferInfo, buffer)
                        buffer.clear()
                        flush()
                    }
                    codec!!.releaseOutputBuffer(index, false)
                }
            }
        })
    }

    override fun getSampleSize(): Int {
        if (!codecStarted) {
            synchronized(lock) {
                try {
                    lock.wait()
                } catch (e: InterruptedException) {

                }
            }
        }
        synchronized(this@AudioDecoderImpl) {
            if (decode()) {
                while (true) {
                    val index = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
                    return if (index >= 0) {
                        if (sampleSize <= 0) {
                            sampleSize = bufferInfo.size
                        }
                        val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            codec!!.getOutputBuffer(index)
                        } else {
                            codec!!.outputBuffers[index]
                        }
                        buffer.clear()
                        codec!!.releaseOutputBuffer(index, false)
                        sampleSize
                    } else {
                        getSampleSize()
                    }
                }
            }
        }
        return 0
    }

    private fun next() {
//        val delay = if (forPlay) {
//            val d = bufferInfo.presentationTimeUs / 1000 - lastPts
//            lastPts = bufferInfo.presentationTimeUs / 1000
//            d
//        } else 0
        pipeline?.queueEvent(Runnable {
            synchronized(this@AudioDecoderImpl) {
                if (!starting) return@Runnable
                if (decode()) {
                    dequeue()
                }
//            debug_i("next ${videoInfo.presentationTimeUs}")
                if (!eos) {
                    next()
                } else {
                    flush()
                }
            }
        })
    }

    private fun decode(): Boolean {
        val index = codec!!.dequeueInputBuffer(WAIT_TIME)
        if (index >= 0) {
            val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                codec!!.getInputBuffer(index)
            } else {
                codec!!.inputBuffers[index]
            }
            val size = track.readSampleData(buffer, 0)
            if (size < 0) {
                codec!!.queueInputBuffer(index, 0, 0, 0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                debug_e("eos!")
                eos = true
                starting = false
            } else {
                codec!!.queueInputBuffer(index, 0, size, track.getSampleTime(), 0)
                track.advance()
            }
//            track.unselect()
            return true
        } else {
            debug_e("Cannot get input buffer!")
        }
        return false
    }

    override fun start() {
        if (eos) {
            debug_i("EOS!")
            return
        }
        starting = true
        onStateListener?.onStart(this)
        next()
    }

    override fun pause() {
        if (eos) {
            debug_i("EOS!")
            return
        }
        starting = false
        onStateListener?.onPause(this)
    }

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
        pipeline?.quit()
        pipeline = null
        mDequeuePipeline?.quit()
        mDequeuePipeline = null
    }

    override fun getDuration(): Int = track.format.getInteger(MediaFormat.KEY_DURATION) / 1000

    override fun post(event: Runnable) {
        pipeline?.queueEvent(event)
    }

    /**
     * {@link https://blog.csdn.net/supermanwg/article/details/52798445}
     * {@link https://blog.csdn.net/jay100500/article/details/52955232}
     */
    override fun getSampleRate(): Int {
        val sampleRate = when (track.format.getString(MediaFormat.KEY_MIME)) {
            MediaFormat.MIMETYPE_AUDIO_AAC -> {
                when (track.format.getInteger(MediaFormat.KEY_AAC_PROFILE)) {
                    0 -> track.format.getInteger(MediaFormat.KEY_SAMPLE_RATE) * 2
                    else -> track.format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                }
//                val csd0 = track.format.getByteBuffer("csd-0")
//                csd0.rewind()
//                val data = ByteArray(csd0.capacity())
//                csd0.get(data, 0, data.size)
//                csd0.rewind()
//                val h = data[0] and 0x7
//                val l = data[1].toInt().shr(7) and 0x1
//                val index = h.toInt().shl(1) and 0x0e or l
//                AudioDecoder.AAC_SAMPLING_FREQUENCIES[index]
            }
            else -> track.format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        }
        debug_e("Sample rate: $sampleRate")
        return sampleRate
    }

    override fun getChannel(): Int = track.format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

    companion object {
        private const val WAIT_TIME = 10000L
    }
}