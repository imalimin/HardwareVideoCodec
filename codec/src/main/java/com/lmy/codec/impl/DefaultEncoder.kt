package com.lmy.codec.impl

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.Encoder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.texture.impl.BaseTexture
import com.lmy.codec.texture.impl.NormalTexture
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_v
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.CodecTextureWrapper


/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class DefaultEncoder(var parameter: Parameter,
                     var cameraWrapper: CameraTextureWrapper,
                     var codecWrapper: CodecTextureWrapper? = null,
                     private var codec: MediaCodec? = null,
                     private var format: MediaFormat? = null,
                     private var filter: BaseTexture? = null,
                     private var mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo())
    : Encoder {

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
    private var mTimestamp: Long = 0
    private var mPresentationTimeUs: Long = 0

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        format = MediaFormat()
        CodecHelper.initFormat(format!!, parameter)
        initCodec()
        initThread()
        mHandler?.removeMessages(INIT)
        mHandler?.sendEmptyMessage(INIT)
    }

    private fun initCodec() {
        debug_v("create codec: ${format?.getString(MediaFormat.KEY_MIME)}")
        try {
            codec = MediaCodec.createEncoderByType(format?.getString(MediaFormat.KEY_MIME))
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
                        synchronized(mEncodingSyn) {
                            if (mEncoding)
                                encode()
                        }
                    }
                    STOP -> {
                        debug_v("dequeue left frames")
                        //编码结束，发送结束信号，让surface不在提供数据
                        codec!!.signalEndOfInputStream()
                        codec!!.stop()
                        codec!!.release()
//                        dequeue()
                        codecWrapper?.release()
                        val listener = msg.obj
                        if (null != listener)
                            (listener as Encoder.OnStopListener).onStop()
                    }
                }
            }
        }
    }

    private fun init() {
        if (null == codec) {
            debug_e("codec is null")
            return
        }
        codec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codecWrapper = CodecTextureWrapper(codec!!.createInputSurface(), cameraWrapper.egl!!.eglContext)
        filter = NormalTexture(cameraWrapper.getFrameTexture(), cameraWrapper.getDrawer())
        codecWrapper?.setFilter(filter!!)
        codecWrapper?.egl?.makeCurrent()
        codec!!.start()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(mEncodingSyn) {
            if (mEncoding) {
                mHandler?.removeMessages(ENCODE)
                mHandler?.sendEmptyMessage(ENCODE)
            }
        }
    }

    private fun encode() {
        val timestamp = System.currentTimeMillis()
        if (0L != mTimestamp)
            mPresentationTimeUs += (timestamp - mTimestamp) * 1000
        else
            mPresentationTimeUs += 1000000 / parameter.video.fps
        mTimestamp = timestamp
        codecWrapper?.egl?.makeCurrent()
        GLES20.glViewport(0, 0, parameter.video.width, parameter.video.height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        codecWrapper?.drawTexture(null)
        codecWrapper?.egl?.swapBuffers()
        dequeue()
    }

    @SuppressLint("WrongConstant")
    private fun dequeue() {
        try {
            val flag = codec!!.dequeueOutputBuffer(mBufferInfo, WAIT_TIME)
            when (flag) {
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                    debug_v("INFO_OUTPUT_BUFFERS_CHANGED")
                }
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
//                    debug_v("INFO_TRY_AGAIN_LATER")
                }
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    debug_v("INFO_OUTPUT_FORMAT_CHANGED")
                    onSampleListener?.onFormatChanged(codec!!.outputFormat)
                }
                else -> {
                    if (flag < 0) return@dequeue
                    val data = codec!!.outputBuffers[flag]
                    if (null != data) {
                        val endOfStream = mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        if (endOfStream == 0) {
                            mBufferInfo.presentationTimeUs = mPresentationTimeUs
                            onSampleListener?.onSample(mBufferInfo, data)
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
            mTimestamp = 0
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
}