/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.muxer.impl

import android.media.MediaCodec
import android.media.MediaFormat
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.encoder.impl.AudioEncoderImpl
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Sample
import com.lmy.codec.muxer.Muxer
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import java.lang.reflect.Method
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/7/25.
 */
class RtmpMuxerImpl(var context: CodecContext) : Muxer {
    private var client: RtmpReflect = RtmpReflect()

    companion object {
        private val clientLock = Any()
    }

    init {
        start()
    }

    private fun start() {
        synchronized(clientLock) {
            client.connect(context.ioContext.path!!, 10000, 500)
            client.connectStream(context.video.width, context.video.height)
        }
    }

    override fun reset() {
        synchronized(clientLock) {
            debug_i("RTMP reset")
            client.connectStream(context.video.width, context.video.height)
        }
    }

    //分发MediaFormat
    override fun onFormatChanged(encoder: Encoder, format: MediaFormat) {
        if (encoder is AudioEncoderImpl) {
            debug_i("Add audio track")
            addAudioTrack(format)
        } else {
            debug_i("Add video track")
            addVideoTrack(format)
        }
    }

    override fun onSample(encoder: Encoder, info: MediaCodec.BufferInfo, data: ByteBuffer) {
        if (encoder is AudioEncoderImpl) {
            writeAudioSample(Sample.wrap(info, data))
        } else {
            writeVideoSample(Sample.wrap(info, data))
        }
    }

    override fun addVideoTrack(format: MediaFormat) {
        val spsBuffer = format.getByteBuffer("csd-0")
        val ppsBuffer = format.getByteBuffer("csd-1")
        if (null == spsBuffer || null == ppsBuffer) {
            debug_e("Codec-specific data is empty!")
            return
        }
        spsBuffer.rewind()
        ppsBuffer.rewind()
        val sps = ByteArray(spsBuffer.remaining())
        val pps = ByteArray(ppsBuffer.remaining())
        spsBuffer.get(sps)
        ppsBuffer.get(pps)
        spsBuffer.rewind()
        ppsBuffer.rewind()
        synchronized(clientLock) {
            client.sendVideoSpecificData(sps, sps.size, pps, pps.size)
        }
    }

    override fun addAudioTrack(format: MediaFormat) {
        val esdsBuffer = format.getByteBuffer("csd-0")
        if (null == esdsBuffer) {
            debug_e("Codec-specific data is empty!")
            return
        }
        esdsBuffer.rewind()
        val esds = ByteArray(esdsBuffer.remaining())
        esdsBuffer.get(esds)
        esdsBuffer.rewind()
        synchronized(clientLock) {
            client.sendAudioSpecificData(esds, esds.size)
        }
    }

    override fun writeVideoSample(sample: Sample) {
        val data = ByteArray(sample.bufferInfo.size)
        sample.sample.rewind()
        sample.sample.get(data)
        sample.sample.rewind()
        synchronized(clientLock) {
            client.sendVideo(data, data.size, sample.bufferInfo.presentationTimeUs / 1000)
        }
    }

    override fun writeAudioSample(sample: Sample) {
        val data = ByteArray(sample.bufferInfo.size)
        sample.sample.rewind()
        sample.sample.get(data)
        sample.sample.rewind()
        synchronized(clientLock) {
            client.sendAudio(data, data.size, sample.bufferInfo.presentationTimeUs / 1000)
        }
    }

    override fun release() {
        synchronized(clientLock) {
            client.stop()
        }
    }

    inner class RtmpReflect {
        private val clazz: Class<*>
        private val thiz: Any
        private val methodConnect: Method
        private val methodConnectStream: Method
        private val methodSendVideoSpecificData: Method
        private val methodSendVideo: Method
        private val methodSendAudioSpecificData: Method
        private val methodSendAudio: Method
        private val methodStop: Method
        private val methodSetCacheSize: Method

        init {
            try {
                clazz = Class.forName("com.lmy.rtmp.RtmpClient")
                methodConnect = clazz.getMethod("connect", String::class.java, Int::class.java, Int::class.java)
                methodConnectStream = clazz.getMethod("connectStream", Int::class.java, Int::class.java)
                methodSendVideoSpecificData = clazz.getMethod("sendVideoSpecificData",
                        ByteArray::class.java, Int::class.java, ByteArray::class.java, Int::class.java)
                methodSendVideo = clazz.getMethod("sendVideo", ByteArray::class.java,
                        Int::class.java, Long::class.java)
                methodSendAudioSpecificData = clazz.getMethod("sendAudioSpecificData",
                        ByteArray::class.java, Int::class.java)
                methodSendAudio = clazz.getMethod("sendAudio", ByteArray::class.java,
                        Int::class.java, Long::class.java)
                methodStop = clazz.getMethod("stop")
                methodSetCacheSize = clazz.getMethod("setCacheSize", Int::class.java)
                thiz = clazz.newInstance()
            } catch (e: ClassNotFoundException) {
                throw RuntimeException("If you want to use RTMP stream. Please implementation 'com.lmy.codec:rtmp:latestVersion'")
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
                throw RuntimeException("Make sure you are using the correct version of the rtmp library.")
            }
        }

        fun connect(url: String, timeOutMs: Int, cacheSize: Int): Int {
            return methodConnect.invoke(thiz, url, timeOutMs, cacheSize) as Int
        }

        fun connectStream(width: Int, height: Int): Int {
            return methodConnectStream.invoke(thiz, width, height) as Int
        }

        fun sendVideoSpecificData(sps: ByteArray, spsLen: Int, pps: ByteArray, ppsLen: Int): Int {
            return methodSendVideoSpecificData.invoke(thiz, sps, spsLen, pps, ppsLen) as Int
        }

        fun sendVideo(data: ByteArray, len: Int, timestamp: Long): Int {
            return methodSendVideo.invoke(thiz, data, len, timestamp) as Int
        }

        fun sendAudioSpecificData(data: ByteArray, len: Int): Int {
            return methodSendAudioSpecificData.invoke(thiz, data, len) as Int
        }

        fun sendAudio(data: ByteArray, len: Int, timestamp: Long): Int {
            return methodSendAudio.invoke(thiz, data, len, timestamp) as Int
        }

        fun stop() {
            methodStop.invoke(thiz)
        }

        fun setCacheSize(size: Int) {
            methodSetCacheSize.invoke(thiz, size)
        }
    }
}