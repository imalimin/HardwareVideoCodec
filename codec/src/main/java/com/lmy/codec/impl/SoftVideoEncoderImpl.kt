package com.lmy.codec.impl

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.GLES20
import android.os.Build
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
                           private var srcBuffer: ByteBuffer? = null,
                           private var mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo(),
                           private var pTimer: VideoEncoderImpl.PresentationTimer = VideoEncoderImpl.PresentationTimer(parameter.video.fps)) : Encoder {

    companion object {
        val HEADER: Array<Byte> = arrayOf(0, 0, 0, 1, 103, 100, 0, 30, -84, -46, 2, -48, -10, -102, -126, -125, 2, -125, 104, 80, -102, -128, 0, 0, 0, 1, 104, -18, 6, -30, -64)
        val INIT = 0x1
        val ENCODE = 0x2
        val STOP = 0x3

        const val BUFFER_FLAG_KEY_FRAME = 1
        const val BUFFER_FLAG_CODEC_CONFIG = 2
        const val BUFFER_FLAG_END_OF_STREAM = 4
        const val BUFFER_FLAG_PARTIAL_FRAME = 8
    }

    private var mHandlerThread = HandlerThread("Encode_Thread")
    private var mHandler: Handler? = null
    private val mEncodingSyn = Any()
    private var mEncoding = false
    private var mFrameCount = 0

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        initCodec()
        initThread()
        srcBuffer = ByteBuffer.allocate(720 * 480 * 3)
        srcBuffer?.order(ByteOrder.nativeOrder())
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

    private fun getOutFormat(): MediaFormat {
        val out = MediaFormat()
        out.setString(MediaFormat.KEY_MIME, format.getString(MediaFormat.KEY_MIME))
        out.setInteger(MediaFormat.KEY_WIDTH, format.getInteger(MediaFormat.KEY_WIDTH))
        out.setInteger(MediaFormat.KEY_HEIGHT, format.getInteger(MediaFormat.KEY_HEIGHT))
        out.setInteger(MediaFormat.KEY_BIT_RATE, format.getInteger(MediaFormat.KEY_BIT_RATE))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            out.setInteger(MediaFormat.KEY_COLOR_RANGE, 2)
            out.setInteger(MediaFormat.KEY_COLOR_STANDARD, 4)
            out.setInteger(MediaFormat.KEY_COLOR_TRANSFER, 3)
        }
        return out
    }

    private fun initThread() {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    INIT -> {
                        pTimer.reset()
                        onSampleListener?.onFormatChanged(getOutFormat())
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

    private fun encode() {
        ++mFrameCount
        pTimer.record()
        if (srcBuffer == null) return
        val time = System.currentTimeMillis()
        val size = codec?.encode(srcBuffer!!.array(), srcBuffer!!.capacity())!!
        if (0 == size) return
//        if (1 == mFrameCount) {
//            mBufferInfo.size = 31
//            mBufferInfo.presentationTimeUs = pTimer.presentationTimeUs
//            mBufferInfo.flags = BUFFER_FLAG_CODEC_CONFIG
//            val byteArray = ByteArray(mBufferInfo.size)
//            HEADER.forEachIndexed { index, byte ->
//                byteArray[index] = byte
//            }
//            onSampleListener?.onSample(mBufferInfo, ByteBuffer.wrap(byteArray, 0, mBufferInfo.size))
//            pTimer.record()
//            return
//        }
        mBufferInfo.presentationTimeUs = pTimer.presentationTimeUs
        mBufferInfo.size = size
        when (codec!!.getType()) {
            1 -> mBufferInfo.flags = BUFFER_FLAG_KEY_FRAME
            else -> mBufferInfo.flags = 0
        }
        codec!!.buffer!!.position(0)
        codec!!.buffer!!.limit(size)
        onSampleListener?.onSample(mBufferInfo, ByteBuffer.wrap(codec!!.buffer!!.array(), 0, mBufferInfo.size))
        debug_e("x264 frame size = $size, cost ${System.currentTimeMillis() - time}ms")
    }

    private fun readPixels() {
        if (0 != mFrameCount % 24) return
        srcBuffer!!.clear()
        srcBuffer!!.position(0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, cameraWrapper.getFrameBuffer())
        GLES20.glReadPixels(0, 576, 720, 480, GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_BYTE, srcBuffer!!)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        debug_e("buffer[${srcBuffer!![2000]}, ${srcBuffer!![2001]}, ${srcBuffer!![2002]}, ${srcBuffer!![2003]}]")

//        val bitmap = Bitmap.createBitmap(bmpBuffer!!.array(), 720, 480, Bitmap.Config.RGB_565)
//        if (null == bitmap) {
//            debug_e("Bitmap is null")
//            return
//        }
//        val out = FileOutputStream("/storage/emulated/0/000.jpg")
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
//        debug_e("Saved!")
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
                readPixels()
                mHandler?.removeMessages(VideoEncoderImpl.ENCODE)
                mHandler?.sendEmptyMessage(VideoEncoderImpl.ENCODE)
            }
        }
    }
}