/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.encoder.impl

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.RecycleQueue
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.media.AudioRecorder
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_v
import java.nio.ByteBuffer

/**
 * Created by 李明艺 on 2018/3/31.
 * Project Name：HardwareVideoCodec.
 * @author lrlmy@foxmail.com
 */
class AudioEncoderImpl private constructor(var context: CodecContext,
                                           private var bufferSize: Int = 0)
    : Encoder, AudioRecorder.OnPCMListener {

    companion object {
        private val WAIT_TIME = 10000L
        fun fromDevice(context: CodecContext): Encoder {
            return AudioEncoderImpl(context)
        }

        fun fromArray(context: CodecContext, bufferSize: Int): Encoder {
            return AudioEncoderImpl(context, bufferSize)
        }
    }

    override var onPreparedListener: Encoder.OnPreparedListener? = null
    override var onRecordListener: Encoder.OnRecordListener? = null
    private val outputFormatLock = Object()
    private var audioRecorder: AudioRecorder? = null
    private var codec: MediaCodec? = null
    private var inputBuffers: Array<ByteBuffer>? = null
    private var outputBuffers: Array<ByteBuffer>? = null
    private var bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private var pTimer: PresentationTimer = PresentationTimer(context.audio.sampleRateInHz)
    private lateinit var format: MediaFormat
    private var mPipeline = EventPipeline.create("AudioEncodePipeline")
    private var mDequeuePipeline = EventPipeline.create("AudioDequeuePipeline")
    private val mEncodingSyn = Any()
    private var mEncoding = false
    private var onSampleListener: Encoder.OnSampleListener? = null
    private var mCache: Cache? = null
    private var looping = false

    init {
        bufferSize = if (bufferSize > 0) {
            bufferSize
        } else {
            audioRecorder = AudioRecorder(context)
            audioRecorder?.setOnPCMListener(this)
            audioRecorder!!.getBufferSize()
        }
        mPipeline.queueEvent(Runnable {
            initCodec()
            init()
        })
    }

    private fun initCodec() {
        val f = CodecHelper.createAudioFormat(context, bufferSize)
        if (null == f) {
            debug_e("Unsupport codec type")
            return
        }
        format = f!!
        debug_v("create codec: ${format.getString(MediaFormat.KEY_MIME)}")
        try {
            codec = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME))
        } catch (e: Exception) {
            debug_e("Can not create codec")
        } finally {
            if (null == codec)
                debug_e("Can not create codec")
        }
    }

    private fun init() {
        pTimer.reset()
        codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec?.start()
        mCache = Cache(5, bufferSize)
        mCache?.ready()
    }

    override fun getOutputFormat(): MediaFormat {
        mPipeline.queueEvent(Runnable {
            var index = 0
            while (MediaCodec.INFO_OUTPUT_FORMAT_CHANGED != index) {
                index = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
            }
            synchronized(outputFormatLock) {
                outputFormatLock.notifyAll()
            }
        })
        synchronized(outputFormatLock) {
            try {
                outputFormatLock.wait()
            } catch (e: InterruptedException) {

            }
        }
        return codec!!.outputFormat
    }

    private val looper = Runnable {
        if (!dequeue()) {
            try {
                Thread.sleep(5)
            } catch (e: InterruptedException) {

            }
        }
        loop()
    }

    private fun loop() {
        if (!looping) return
        mDequeuePipeline.queueEvent(looper)
    }

    override fun onPCMSample(buffer: ByteArray) {
        if (!mEncoding || null == mCache) return
        if (!looping) {
            looping = true
            loop()
        }
        val cache = mCache!!.pollCache() ?: return
        System.arraycopy(buffer, 0, cache, 0, buffer.size)
        mCache!!.offer(cache)
        mPipeline.queueEvent(Runnable { encode() })
    }

    override fun setPresentationTime(nsecs: Long) {

    }

    private fun encode() {
        try {
            val cache = mCache!!.take()
            encode(cache)
            mCache!!.recycle(cache)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun encode(buffer: ByteArray) {
        synchronized(mEncodingSyn) {
            try {
                inputBuffers = codec!!.inputBuffers
                outputBuffers = codec!!.outputBuffers
                val inputBufferIndex = codec!!.dequeueInputBuffer(WAIT_TIME)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = inputBuffers!![inputBufferIndex]
                    inputBuffer.clear()
                    inputBuffer.put(buffer)
                    codec!!.queueInputBuffer(inputBufferIndex, 0, buffer.size, 0, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("WrongConstant", "SwitchIntDef")
    private fun dequeue(): Boolean {
        try {
            val flag = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
            when (flag) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    return false
                }
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    debug_v("AUDIO INFO_OUTPUT_FORMAT_CHANGED")
                    onSampleListener?.onFormatChanged(this, codec!!.outputFormat)
                }
                else -> {
                    if (flag < 0) return@dequeue false
                    val data = codec!!.outputBuffers[flag]
                    if (null != data) {
                        val endOfStream = bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        if (endOfStream == 0 && bufferInfo.size > 0) {
//                            bufferInfo.presentationTimeUs = pTimer.presentationTimeUs
//                            debug_e("read sample($flag): ${bufferInfo.size}")
//                            if (bufferInfo.presentationTimeUs > 0)
//                            timestamp += 29023
//                            bufferInfo.presentationTimeUs = timestamp
                            pTimer.record()
                            bufferInfo.presentationTimeUs = pTimer.presentationTimeUs
                            onSampleListener?.onSample(this, CodecHelper.copy(bufferInfo), data)
                        }
                        // 一定要记得释放
                        codec!!.releaseOutputBuffer(flag, false)
//                        if (endOfStream == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
//                            return true
//                        }
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
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
//        while (dequeue()) {//取出编码器中剩余的帧
//        }
        looping = false
        mPipeline.quit()
        mDequeuePipeline.quit()
        codec!!.stop()
        codec!!.release()
        audioRecorder?.stop()
        mCache?.release()
        debug_e("Audio encoder stop")
    }

    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    @Deprecated("Invalid")
    override fun onFrameAvailable(p0: SurfaceTexture?) {
    }

    class PresentationTimer(var sampleRateInHz: Int,
                            var presentationTimeUs: Long = 0,
                            var highPrecisionTimeUs: Double = 0.0,
                            private var interval: Double = 0.0) {

        init {
            interval = 1000000000L / sampleRateInHz.toDouble()
        }

        fun start() {
        }

        fun record() {
            highPrecisionTimeUs += interval
            presentationTimeUs = highPrecisionTimeUs.toLong()
        }

        fun reset() {
            presentationTimeUs = 0
            highPrecisionTimeUs = 0.0
        }
    }

    private class Cache(capacity: Int, private val bufferSize: Int) : RecycleQueue<ByteArray>(capacity) {
        override fun newCacheEntry(): ByteArray {
            return ByteArray(bufferSize)
        }

    }
}