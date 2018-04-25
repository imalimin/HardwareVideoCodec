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
    init {
        System.loadLibrary("x264")
        System.loadLibrary("codec")
        init("veryfast", "zerolatency")
        setVideoSize(format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT))
        setBitrate(format.getInteger(MediaFormat.KEY_BIT_RATE))
        setFrameFormat(FrameFormat.X264_CSP_I420)
        setFps(format.getInteger(MediaFormat.KEY_FRAME_RATE))
        buffer = ByteBuffer.allocate(720 * 480 * 3)
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