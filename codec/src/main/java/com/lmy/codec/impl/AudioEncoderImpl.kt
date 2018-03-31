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
                       private var audioWrapper: AudioRecordWrapper? = null)
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
                    VideoEncoderImpl.INIT -> {
                        init()
                    }
                    VideoEncoderImpl.ENCODE -> {
                        encode(msg.obj as ByteArray)
                    }
                    VideoEncoderImpl.STOP -> {
                        //编码结束，发送结束信号，让surface不在提供数据
                        codec!!.signalEndOfInputStream()
                        codec!!.stop()
                        codec!!.release()
                        audioWrapper?.stop()
                        val listener = msg.obj
                        if (null != listener)
                            (listener as Encoder.OnStopListener).onStop()
                    }
                }
            }
        }
    }

    private fun init() {
        codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec?.start()
        audioWrapper = AudioRecordWrapper(parameter)
        audioWrapper?.setOnPCMListener(this)
    }

    private fun encode(buffer: ByteArray) {
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
            dequeue()
//            var outputBufferIndex = codec!!.dequeueOutputBuffer(bufferInfo, 0)
//            while (outputBufferIndex >= 0) {
//                val outputBuffer = outputBuffers!![outputBufferIndex]
////                outputBuffer.position(bufferInfo.offset)
//                onSampleListener?.onSample(bufferInfo, outputBuffer)
//                codec!!.releaseOutputBuffer(outputBufferIndex, false)
//                outputBufferIndex = codec!!.dequeueOutputBuffer(bufferInfo, 0)
//            }
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
    private fun dequeue() {
        try {
            val flag = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
            when (flag) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    debug_v("AUDIO INFO_OUTPUT_FORMAT_CHANGED")
                    onSampleListener?.onFormatChanged(codec!!.outputFormat)
                }
                else -> {
                    if (flag < 0) return@dequeue
                    val data = codec!!.outputBuffers[flag]
                    if (null != data) {
                        val endOfStream = bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        if (endOfStream == 0) {
//                            bufferInfo.presentationTimeUs = pTimer.presentationTimeUs
//                            debug_e("read sample($flag): ${bufferInfo.size}")
                            onSampleListener?.onSample(bufferInfo, data)
                        }
                        // 一定要记得释放
                        codec!!.releaseOutputBuffer(flag, false)
                        if (endOfStream == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            return
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun start() {
        synchronized(mEncodingSyn) {
            //            pTimer.start()
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
        audioWrapper?.stop()
        mHandler?.removeMessages(VideoEncoderImpl.STOP)
        mHandler?.sendMessage(mHandler!!.obtainMessage(VideoEncoderImpl.STOP, listener))
    }

    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    @Deprecated("Invalid")
    override fun onFrameAvailable(p0: SurfaceTexture?) {
    }
}