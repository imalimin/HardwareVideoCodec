package com.lmy.codec.impl

import android.graphics.SurfaceTexture
import android.media.MediaFormat
import android.opengl.GLES20
import android.opengl.GLES30
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.Encoder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.x264.X264Encoder
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by lmyooyo@gmail.com on 2018/4/3.
 */
class SoftVideoEncoderImpl(var parameter: Parameter,
                           var cameraWrapper: CameraTextureWrapper,
                           var codec: X264Encoder? = null,
                           private var format: MediaFormat = MediaFormat(),
                           private var buffer: ByteBuffer? = null,
                           private var pTimer: VideoEncoderImpl.PresentationTimer = VideoEncoderImpl.PresentationTimer(parameter.video.fps)) : Encoder {

    companion object {
        val INIT = 0x1
        val ENCODE = 0x2
        val STOP = 0x3
    }

    private var mHandlerThread = HandlerThread("Encode_Thread")
    private var mHandler: Handler? = null
    private val mEncodingSyn = Any()
    private var mEncoding = false

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        initCodec()
        initThread()
        mHandler?.removeMessages(VideoEncoderImpl.INIT)
        mHandler?.sendEmptyMessage(VideoEncoderImpl.INIT)
    }

    private fun initCodec() {
        CodecHelper.initFormat(format, parameter)
        codec = X264Encoder(format)
        codec?.setProfile("high")
        codec?.setLevel(31)
        codec?.start()
    }

    private fun initThread() {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    INIT -> {
                        pTimer.reset()
                        buffer = ByteBuffer.allocate(parameter.video.width * parameter.video.height * 4)
                        buffer?.order(ByteOrder.nativeOrder())
                    }
                    ENCODE -> {
                        synchronized(mEncodingSyn) {
                            if (mEncoding)
                                encode()
                        }
                    }
                    STOP -> {
                        mHandlerThread.quitSafely()
                        val listener = msg.obj
                        if (null != listener)
                            (listener as Encoder.OnStopListener).onStop()
                    }
                }
            }
        }
    }

    private var isFinish = true
    private fun encode() {
        pTimer.record()
        if (buffer == null || !isFinish) return
        isFinish = false
        buffer?.clear()
//        debug_e("check: ${GLES20.glCheckFramebufferStatus(cameraWrapper.getFrameBuffer())}, ${GLES20.glGetError()}")
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, cameraWrapper.getFrameBuffer())
        GLES20.glReadPixels(0, 0, parameter.video.width, parameter.video.height, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, buffer!!)
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        debug_e("buffer(${cameraWrapper.getFrameBuffer()})[${buffer!![1000]}, ${buffer!![1003]}, ${buffer!![1006]}, ${buffer!![1009]}]")
//        val options = BitmapFactory.Options()
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888
//        val bitmap = BitmapFactory.decodeByteArray(buffer!!.array(), 0, buffer!!.capacity(), options)
//        if (null == bitmap) {
//            debug_e("Bitmap is null")
//            isFinish = true
//            return
//        }
//        val out = FileOutputStream("/storage/emulated/0/000.jpg")
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
//        debug_e("Saved!")
        isFinish = true
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
        mHandler?.removeMessages(VideoEncoderImpl.STOP)
        mHandler?.sendMessage(mHandler!!.obtainMessage(VideoEncoderImpl.STOP, listener))
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(mEncodingSyn) {
            if (mEncoding) {
                mHandler?.removeMessages(VideoEncoderImpl.ENCODE)
                mHandler?.sendEmptyMessage(VideoEncoderImpl.ENCODE)
            }
        }
    }
}