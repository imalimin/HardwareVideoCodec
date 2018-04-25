package com.lmy.codec.x264

import android.media.MediaFormat
import com.lmy.codec.util.debug_e
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by lmyooyo@gmail.com on 2018/4/3.
 */
class X264Encoder(private var format: MediaFormat,
                  var buffer: ByteBuffer? = null,
                  private var type: Int = -1) {
    companion object {
        private const val PRESET = "veryfast"
        private const val TUNE = "zerolatency"
    }

    init {
        System.loadLibrary("x264")
        System.loadLibrary("codec")
        initCacheBuffer()
        init(PRESET, TUNE)
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

    private fun createBuffer(size: Int): ByteArray {
        debug_e("Create buffer($size)")
        return buffer!!.array()
    }

    private fun setType(type: Int) {
//        debug_e("setType($type)")
        this.type = type
    }

    fun getType(): Int {
        return this.type
    }

    fun encode(src: ByteArray, srcSize: Int): Int {
        buffer?.clear()
        buffer?.position(0)
        return encode(src, srcSize, buffer!!.array())
    }

    fun getWidth(): Int {
        return format.getInteger(MediaFormat.KEY_WIDTH)
    }

    fun getHeight(): Int {
        return format.getInteger(MediaFormat.KEY_HEIGHT)
    }

    private external fun init(preset: String, tune: String)
    external fun start()
    external fun stop()
    external fun encode(src: ByteArray, srcSize: Int, out: ByteArray): Int
    external fun setVideoSize(width: Int, height: Int)
    external fun setBitrate(bitrate: Int)
    external fun setFrameFormat(format: Int)
    external fun setFps(fps: Int)
    external fun setProfile(profile: String)
    external fun setLevel(level: Int)
}