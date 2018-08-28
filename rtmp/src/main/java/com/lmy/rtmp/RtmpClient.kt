package com.lmy.rtmp

import android.os.Handler
import android.os.Message

/**
 * Created by lmyooyo@gmail.com on 2018/7/25.
 */
class RtmpClient(private var handler: Handler? = null) : Rtmp {

    init {
        System.loadLibrary("rtmp")
    }

    override fun onJniError(error: Int) {
        val m: Message = when (error) {
            0x100 -> Message.obtain(handler, error, "Connect failed!")
            0x101 -> Message.obtain(handler, error, "Connect stream failed!")
            else -> Message.obtain(handler, error, "Unknown error!")
        }
        handler?.sendMessage(m)
    }

    override fun setHandler(h: Handler) {
        this.handler = h
    }

    external override fun connect(url: String, timeOut: Int, cacheSize: Int): Int
    external override fun connectStream(width: Int, height: Int): Int
    external override fun sendVideoSpecificData(sps: ByteArray, spsLen: Int, pps: ByteArray, ppsLen: Int): Int
    external override fun sendVideo(data: ByteArray, len: Int, timestamp: Long): Int
    external override fun sendAudioSpecificData(data: ByteArray, len: Int): Int
    external override fun sendAudio(data: ByteArray, len: Int, timestamp: Long): Int
    external override fun stop()
    external override fun setCacheSize(size: Int)

    companion object {
        fun build(): RtmpClient {
            return RtmpClient()
        }
    }
}