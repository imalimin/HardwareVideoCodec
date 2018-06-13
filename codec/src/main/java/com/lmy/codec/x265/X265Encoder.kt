package com.lmy.codec.x265

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import com.lmy.codec.util.debug_e
import com.lmy.codec.x264.FrameFormat
import com.lmy.codec.x264.X264
import com.lmy.codec.x264.X264Encoder
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by lmyooyo@gmail.com on 2018/6/13.
 */
class X265Encoder(private var format: MediaFormat,
                  var buffer: ByteBuffer? = null,
                  private var size: Int = 0,
                  private var type: Int = -1,
                  private var ppsLength: Int = 0,
                  private var outFormat: MediaFormat? = null,
                  private var mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()) : X264 {
    companion object {
        private val CSD_0 = "csd-0"
        private val CSD_1 = "csd-1"

        const val BUFFER_FLAG_KEY_FRAME = 1
        const val BUFFER_FLAG_CODEC_CONFIG = 2
        const val BUFFER_FLAG_END_OF_STREAM = 4
        const val BUFFER_FLAG_PARTIAL_FRAME = 8

        init {
            System.loadLibrary("x265")
            System.loadLibrary("codec")
        }
    }

    private var mTotalCost = 0L
    private var mFrameCount = 0

    init {
        initCacheBuffer()
        init()
        setVideoSize(getWidth(), getHeight())
        setBitrate(format.getInteger(MediaFormat.KEY_BIT_RATE))
        setFrameFormat(FrameFormat.X264_CSP_I420)
        setFps(format.getInteger(MediaFormat.KEY_FRAME_RATE))
    }

    /**
     * 初始化缓存，大小为width*height
     * 如果是别的编码格式，缓存大小可能需要增大
     */
    private fun initCacheBuffer() {
        buffer = ByteBuffer.allocate(getWidth() * getHeight())
        buffer?.order(ByteOrder.nativeOrder())
    }

    private fun wrapBufferInfo(size: Int) {
        when (type) {
            -1 -> mBufferInfo.flags = BUFFER_FLAG_CODEC_CONFIG
            1 -> mBufferInfo.flags = BUFFER_FLAG_KEY_FRAME//X264_TYPE_IDR
            2 -> mBufferInfo.flags = BUFFER_FLAG_KEY_FRAME//X264_TYPE_I
            else -> mBufferInfo.flags = 0
        }
        mBufferInfo.size = size
    }

    private fun getOutFormat(info: MediaCodec.BufferInfo, data: ByteBuffer) {
        data.position(0)
        val specialData = ByteArray(info.size)
        data.get(specialData, 0, specialData!!.size)
        outFormat = MediaFormat()
        outFormat?.setString(MediaFormat.KEY_MIME, format.getString(MediaFormat.KEY_MIME))
        outFormat?.setInteger(MediaFormat.KEY_WIDTH, format.getInteger(MediaFormat.KEY_WIDTH))
        outFormat?.setInteger(MediaFormat.KEY_HEIGHT, format.getInteger(MediaFormat.KEY_HEIGHT))
        outFormat?.setInteger(MediaFormat.KEY_BIT_RATE, format.getInteger(MediaFormat.KEY_BIT_RATE))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            outFormat?.setInteger(MediaFormat.KEY_COLOR_RANGE, 2)
            outFormat?.setInteger(MediaFormat.KEY_COLOR_STANDARD, 4)
            outFormat?.setInteger(MediaFormat.KEY_COLOR_TRANSFER, 3)
        }
        val spsAndPps = parseSpecialData(specialData) ?: throw RuntimeException("Special data is empty")
        ppsLength = spsAndPps[0].size
        outFormat?.setByteBuffer(CSD_0, ByteBuffer.wrap(spsAndPps[0]))
        outFormat?.setByteBuffer(CSD_1, ByteBuffer.wrap(spsAndPps[1]))
    }

    private fun parseSpecialData(specialData: ByteArray): Array<ByteArray>? {
        val index = (4 until specialData.size - 4).firstOrNull { isFlag(specialData, it) }
                ?: 0
        if (0 == index) return null
        return arrayOf(specialData.copyOfRange(0, index),
                specialData.copyOfRange(index, specialData.size))
    }

    private fun isFlag(specialData: ByteArray, index: Int): Boolean {
        return 0 == specialData[index].toInt()
                && 0 == specialData[index + 1].toInt()
                && 0 == specialData[index + 2].toInt()
                && 1 == specialData[index + 3].toInt()
    }

    override fun encode(src: ByteArray): MediaCodec.BufferInfo? {
        val time = System.currentTimeMillis()
        ++mFrameCount
        buffer?.clear()
        buffer?.position(0)
        val ret = encode(src, buffer!!.array(), size, type)
        if (!ret) {
            debug_e("Encode failed. size = $size")
            return null
        }
        val cost = System.currentTimeMillis() - time
        mTotalCost += cost
        if (0 == mFrameCount % 20)
            debug_e("x264 frame size = $size, cost ${cost}ms, arg cost ${mTotalCost / mFrameCount}ms")
        wrapBufferInfo(size)
        if (X264Encoder.BUFFER_FLAG_CODEC_CONFIG == mBufferInfo.flags) {
            //获取SPS，PPS
            getOutFormat(mBufferInfo, buffer!!)
            return mBufferInfo
        } else {
            buffer!!.position(0)
            buffer!!.limit(size)
            return mBufferInfo
        }
    }

    fun getOutFormat(): MediaFormat {
        return outFormat!!
    }

    fun getOutBuffer(): ByteBuffer {
        return ByteBuffer.wrap(buffer!!.array(), 0, mBufferInfo.size)
    }

    override fun getWidth(): Int {
        return format.getInteger(MediaFormat.KEY_WIDTH)
    }

    override fun getHeight(): Int {
        return format.getInteger(MediaFormat.KEY_HEIGHT)
    }

    override fun release() {
        stop()
        buffer = null
        outFormat = null
    }

    external fun init()
    external fun start()
    override external fun stop()
    external fun encode(src: ByteArray, dest: ByteArray, size: Int, type: Int): Boolean
    external fun setVideoSize(width: Int, height: Int)
    external fun setBitrate(bitrate: Int)
    external fun setFrameFormat(format: Int)
    external fun setFps(fps: Int)
}