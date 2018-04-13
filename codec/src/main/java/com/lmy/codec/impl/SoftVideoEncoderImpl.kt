package com.lmy.codec.impl

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.opengl.GLES30
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.Encoder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_v
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.x264.X264Encoder
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/4/3.
 */
class SoftVideoEncoderImpl(var parameter: Parameter,
                           var cameraWrapper: CameraTextureWrapper,
                           var codec: X264Encoder? = null,
                           private var specialData: SpecialData? = null,
                           private var pbos: IntArray = IntArray(PBO_COUNT),
                           private var format: MediaFormat = MediaFormat(),
                           private var srcBuffer: ByteBuffer? = null,
                           private var mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo(),
                           private var pTimer: VideoEncoderImpl.PresentationTimer = VideoEncoderImpl.PresentationTimer(parameter.video.fps)) : Encoder {

    companion object {
        val PBO_COUNT = 2
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
    //For PBO
    private var index = 0
    private var nextIndex = 1
    private var inited = false

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        initCodec()
        initThread()
        initPBOs()
        mHandler?.removeMessages(VideoEncoderImpl.INIT)
        mHandler?.sendEmptyMessage(VideoEncoderImpl.INIT)
    }

    private fun initCodec() {
        CodecHelper.initFormat(format, parameter)
        specialData = SpecialData(format, parameter)
        codec = X264Encoder(format)
        codec?.setProfile("high")
        codec?.setLevel(31)
        codec?.start()
    }

    private fun initPBOs() {
        val size = parameter.video.width * parameter.video.height * 4
        pbos = IntArray(PBO_COUNT)
        GLES30.glGenBuffers(PBO_COUNT, pbos, 0)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[0])
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_DYNAMIC_READ)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[1])
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_DYNAMIC_READ)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        debug_e("initPBOs(" + pbos[0] + ", " + pbos[1] + ")")
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
                        specialData!!.dequeueOutputFormat(onSampleListener)
                        inited = true
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
        val data = getPixelsData()
        val size = codec?.encode(data, data.size)!!
        if (size <= 0) {
            debug_e("Encode failed. size = $size")
            return
        }
        mBufferInfo.presentationTimeUs = pTimer.presentationTimeUs
        mBufferInfo.size = size
        when (codec!!.getType()) {
            -1 -> mBufferInfo.flags = BUFFER_FLAG_CODEC_CONFIG
            1 -> mBufferInfo.flags = BUFFER_FLAG_KEY_FRAME//X264_TYPE_IDR
            2 -> mBufferInfo.flags = BUFFER_FLAG_KEY_FRAME//X264_TYPE_I
            else -> mBufferInfo.flags = 0
        }
        debug_e("x264 frame size = $size, cost ${System.currentTimeMillis() - time}ms")
        codec!!.buffer!!.position(0)
        codec!!.buffer!!.limit(size)
        if (2 == mFrameCount) {
            mBufferInfo.size -= 27
            val data = ByteArray(mBufferInfo.size)
            codec!!.buffer!!.position(27)
            codec!!.buffer!!.get(data, 0, data.size)
            onSampleListener?.onSample(mBufferInfo, ByteBuffer.wrap(data))
            return
        }
        onSampleListener?.onSample(mBufferInfo, ByteBuffer.wrap(codec!!.buffer!!.array(), 0, mBufferInfo.size))
    }

    private fun getPixelsData(): ByteArray {
        val data: ByteArray
        srcBuffer?.position(0)
        if (srcBuffer!!.hasArray()) {
            data = srcBuffer!!.array()
        } else {
            data = ByteArray(srcBuffer!!.capacity())
            srcBuffer!!.get(data)
        }
        return data
    }

    private fun readPixels() {
//        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, cameraWrapper.getFrameBuffer())
//        GLES30.glReadBuffer(GLES30.GL_FRONT);
        //绑定到第一个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[index])
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            GLES30.glReadPixels(0, 576, parameter.video.width, parameter.video.height,
                    GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, 0)
        }
        //绑定到第二个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[nextIndex])
        //glMapBufferRange会等待DMA传输完成，所以需要交替使用pbo
        //映射内存
        srcBuffer = GLES30.glMapBufferRange(GLES30.GL_PIXEL_PACK_BUFFER,
                0, parameter.video.width * parameter.video.height * 4,
                GLES30.GL_MAP_READ_BIT) as ByteBuffer
        //解除映射
        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        //解除绑定PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        //交换索引
        index = (index + 1) % 2
        nextIndex = (nextIndex + 1) % 2
        if (null == srcBuffer) {
            debug_e("PBO is null(${pbos[0]}, ${pbos[1]})")
            return
        }
        debug_e("buffer[${srcBuffer!![2000]}, ${srcBuffer!![2001]}, ${srcBuffer!![2002]}, ${srcBuffer!![2003]}]")
    }


    @Throws(FileNotFoundException::class)
    private fun shotScreen(data: ByteArray, width: Int, height: Int) {
        val dataTmp = IntArray(data.size / 4)
        for (i in dataTmp.indices) {
            dataTmp[i] = Color.argb(data[i * 4 + 3].toInt(), data[i * 4].toInt(),
                    data[i * 4 + 1].toInt(), data[i * 4 + 2].toInt())
        }
        val bitmap = Bitmap.createBitmap(dataTmp, width, height, Bitmap.Config.ARGB_8888)
        if (null == bitmap) {
            debug_e("Bitmap is null")
            return
        }
        val out = FileOutputStream("/storage/emulated/0/000.jpg")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        debug_e("Saved!")
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
            if (mEncoding && inited) {
                readPixels()
                mHandler?.removeMessages(VideoEncoderImpl.ENCODE)
                mHandler?.sendEmptyMessage(VideoEncoderImpl.ENCODE)
            }
        }
    }

    class SpecialData(var format: MediaFormat,
                      var parameter: Parameter,
                      var pps: ByteArray? = null,
                      private var codec: MediaCodec? = null,
                      private var mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()) {
        init {
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            debug_v("Create codec: ${format.getString(MediaFormat.KEY_MIME)}")
            try {
                codec = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME))
            } catch (e: Exception) {
                debug_e("Can not create codec")
                e.printStackTrace()
            } finally {
                if (null == codec)
                    debug_e("Can not create codec")
            }
        }

        @SuppressLint("SwitchIntDef")
        fun dequeueOutputFormat(listener: Encoder.OnSampleListener?) {
            codec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            try {
                codec!!.start()
                offerFrameBuffer()
                while (true) {
                    val flag = codec!!.dequeueOutputBuffer(mBufferInfo, 10000L)
                    when (flag) {
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            debug_e("INFO_OUTPUT_FORMAT_CHANGED")
                            listener?.onFormatChanged(codec!!.outputFormat)
                        }
                        else -> {
                            if (dequeuePPS(flag))
                                return
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun dequeuePPS(flag: Int): Boolean {
            debug_e("dequeuePPS: $flag")
            if (flag < 0) return false
            val data = codec!!.outputBuffers[flag]
            if (null != data) {
                val endOfStream = mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM
                if (endOfStream == 0) {
                    pps = kotlin.ByteArray(mBufferInfo.size)
                    data.limit(pps!!.size)
                    data.get(pps)
                    debug_e("dequeuePPS success: ${mBufferInfo.size}")
                }
                // 一定要记得释放
                codec!!.releaseOutputBuffer(flag, false)
                stop()
                return true
            }
            return false
        }

        private fun offerFrameBuffer() {
            val inputBuffers = codec!!.inputBuffers
            val bufferIndex = codec!!.dequeueInputBuffer(-1)
            val size = parameter.video.width * parameter.video.height * 3 / 2
            //提供一帧数据才能让mediaCodec生成outputFormat
            inputBuffers[bufferIndex].clear()
            inputBuffers[bufferIndex].put(ByteArray(size))
            codec!!.queueInputBuffer(bufferIndex, 0, inputBuffers[bufferIndex].position(), 1, 0)
        }

        private fun stop() {
            codec!!.stop()
            codec!!.release()
        }
    }
}