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
    external fun sendVideoSpecificData(sps: ByteArray, spsLen: Int, pps: ByteArray, ppsLen: Int, timestamp: Long): Int
    external fun sendVideo(data: ByteArray, len: Int, timestamp: Long): Int
    external fun sendAudioSpecificData(data: ByteArray, len: Int): Int
    external fun sendAudio(data: ByteArray, len: Int, timestamp: Long): Int
    external fun stop()

    companion object {
        fun build(): RtmpClient {
            return RtmpClient()
        }
    }
}