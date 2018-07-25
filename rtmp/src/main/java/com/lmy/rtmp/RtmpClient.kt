package com.lmy.rtmp

/**
 * Created by lmyooyo@gmail.com on 2018/7/25.
 */
class RtmpClient {
    init {
        System.loadLibrary("rtmp")
        init()
    }

    private external fun init()
    external fun connect(url: String, width: Int, height: Int, timeOut: Int): Int
    external fun sendSpsAndPps(sps: ByteArray, spsLen: Int, pps: ByteArray, ppsLen: Int, timestamp: Long): Int
    external fun sendVideoData(data: ByteArray, len: Int, timestamp: Long): Int
    external fun sendAacSpec(data: ByteArray, len: Int): Int
    external fun sendAacData(data: ByteArray, len: Int, timestamp: Long): Int
    external fun stop()

    companion object {
        fun build(): RtmpClient {
            return RtmpClient()
        }
    }
}