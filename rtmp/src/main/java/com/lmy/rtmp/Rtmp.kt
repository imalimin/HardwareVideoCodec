package com.lmy.rtmp

interface Rtmp {
    fun init()
    fun connect(url: String, width: Int, height: Int, timeOut: Int): Int
    fun sendVideoSpecificData(sps: ByteArray, spsLen: Int, pps: ByteArray, ppsLen: Int, timestamp: Long): Int
    fun sendVideo(data: ByteArray, len: Int, timestamp: Long): Int
    fun sendAudioSpecificData(data: ByteArray, len: Int): Int
    fun sendAudio(data: ByteArray, len: Int, timestamp: Long): Int
    fun stop()
}