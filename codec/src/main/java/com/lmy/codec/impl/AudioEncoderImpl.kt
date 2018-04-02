package com.lmy.codec.impl

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.Encoder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_v
import com.lmy.codec.wrapper.AudioRecordWrapper
import java.nio.ByteBuffer

/**
 * Created by 李明艺 on 2018/3/31.
 * Project Name：HardwareVideoCodec.
 * @author lrlmy@foxmail.com
 */
class AudioEncoderImpl(var parameter: Parameter,
                       private var codec: MediaCodec? = null,
                       private var format: MediaFormat = MediaFormat(),
                       var inputBuffers: Array<ByteBuffer>? = null,
                       var outputBuffers: Array<ByteBuffer>? = null,
                       private var bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo(),
                       private var audioWrapper: AudioRecordWrapper? = null,
                       private var pTimer: PresentationTimer = PresentationTimer(parameter.video.fps))
    : Encoder, AudioRecordWrapper.OnPCMListener {

    companion object {
        private val WAIT_TIME = 10000L
        val INIT = 0x1
        val ENCODE = 0x2
        val STOP = 0x3
    }

    private var mHandlerThread = HandlerThread("Encode_Thread")
    private var mHandler: Handler? = null
    private val mEncodingSyn = Any()
    private var mEncoding = false
    private var onSampleListener: Encoder.OnSampleListener? = null

    init {
        initCodec()
        initThread()
        mHandler?.removeMessages(INIT)
        mHandler?.sendEmptyMessage(INIT)
    }

    private fun initCodec() {
        CodecHelper.initAudioFormat(format, parameter)
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

    private fun initThread() {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    INIT -> {
                        init()
                    }
                    ENCODE -> {
                        encode(msg.obj as ByteArray)
                    }
                    STOP -> {
                        while (dequeue()) {//取出编码器中剩余的帧
                        }
                        debug_e("Audio encoder stop")
                        codec!!.stop()
                        codec!!.release()
                        audioWrapper?.stop()
                        mHandlerThread.quitSafely()
                        val listener = msg.obj
                        if (null != listener)
                            (listener as Encoder.OnStopListener).onStop()
                    }
                }
            }
        }
    }

    private fun init() {
        pTimer.reset()
        codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec?.start()
        audioWrapper = AudioRecordWrapper(parameter)
        audioWrapper?.setOnPCMListener(this)
    }

    private fun encode(buffer: ByteArray) {
        try {
            pTimer.record()
            inputBuffers = codec!!.inputBuffers
            outputBuffers = codec!!.outputBuffers
            val inputBufferIndex = codec!!.dequeueInputBuffer(WAIT_TIME)
            if (inputBufferIndex >= 0) {
                val inputBuffer = inputBuffers!![inputBufferIndex]
                inputBuffer.clear()
                inputBuffer.put(buffer)
                codec!!.queueInputBuffer(inputBufferIndex, 0, buffer.size, 0, 0)
            }
            dequeue()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPCMSample(buffer: ByteArray) {
        synchronized(mEncodingSyn) {
            if (mEncoding) {
                mHandler?.removeMessages(ENCODE)
                mHandler?.sendMessage(mHandler!!.obtainMessage(ENCODE, buffer))
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
                    onSampleListener?.onFormatChanged(codec!!.outputFormat)
                }
                else -> {
                    if (flag < 0) return@dequeue false
                    val data = codec!!.outputBuffers[flag]
                    if (null != data) {
                        val endOfStream = bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        if (endOfStream == 0) {
//                            bufferInfo.presentationTimeUs = pTimer.presentationTimeUs
//                            debug_e("read sample($flag): ${bufferInfo.size}")
//                            if (bufferInfo.presentationTimeUs > 0)
//                            timestamp += 29023
//                            bufferInfo.presentationTimeUs = timestamp
                            bufferInfo.presentationTimeUs = pTimer.presentationTimeUs
                            onSampleListener?.onSample(bufferInfo, data)
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
        stop(null)
    }

    override fun stop(listener: Encoder.OnStopListener?) {
        pause()
        mHandler?.removeMessages(STOP)
        mHandler?.sendMessage(mHandler!!.obtainMessage(STOP, listener))
    }

    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    @Deprecated("Invalid")
    override fun onFrameAvailable(p0: SurfaceTexture?) {
    }

    class PresentationTimer(var fps: Int,
                            var presentationTimeUs: Long = 0,
                            private var timestamp: Long = 0) {

        fun start() {
            timestamp = 0
        }

        fun record() {
            val timeTmp = System.currentTimeMillis()
            presentationTimeUs += if (0L != timestamp)
                (timeTmp - timestamp) * 1000
            else
                1000000L / fps
            timestamp = timeTmp
        }

        fun reset() {
            presentationTimeUs = 0
            timestamp = 0
        }
    }
}