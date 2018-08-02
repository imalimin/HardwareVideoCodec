package com.lmy.codec.muxer.impl

import android.media.MediaCodec
import android.media.MediaFormat
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.encoder.impl.AudioEncoderImpl
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Sample
import com.lmy.codec.muxer.Muxer
import com.lmy.codec.pipeline.EventPipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import java.lang.reflect.Method
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/7/25.
 */
class RtmpMuxerImpl(var context: CodecContext) : Muxer {
    private var client: RtmpReflect = RtmpReflect()

//    private var mAudioPipeline = EventPipeline.create("LivePipeline")

    init {
        start()
    }

    private fun start() {
        client.connect(context.ioContext.path!!, 10000)
        client.connectStream(context.video.width, context.video.height)
    }

    override fun reset() {
        debug_i("RTMP reset")
        client.connectStream(context.video.width, context.video.height)
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

    private var sent = false
    override fun onSample(encoder: Encoder, info: MediaCodec.BufferInfo, data: ByteBuffer) {
        if (encoder is AudioEncoderImpl) {
            writeAudioSample(Sample.wrap(info, data))
        } else {
            if (sent) return
            sent = true
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
        client.sendVideoSpecificData(sps, sps.size, pps, pps.size)
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
        client.sendAudioSpecificData(esds, esds.size)
    }

    override fun writeVideoSample(sample: Sample) {
        val data = ByteArray(sample.bufferInfo.size)
        sample.sample.rewind()
        sample.sample.get(data)
        sample.sample.rewind()
        client.sendVideo(data, data.size, sample.bufferInfo.presentationTimeUs / 1000)
    }

    override fun writeAudioSample(sample: Sample) {
        val data = ByteArray(sample.bufferInfo.size)
        sample.sample.rewind()
        sample.sample.get(data)
        sample.sample.rewind()
//        client.sendAudio(data, data.size, sample.bufferInfo.presentationTimeUs / 1000)
    }

    override fun release() {
        client.stop()
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

        init {
            try {
                clazz = Class.forName("com.lmy.rtmp.RtmpClient")
                methodConnect = clazz.getMethod("connect", String::class.java, Int::class.java)
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
                thiz = clazz.newInstance()
            } catch (e: ClassNotFoundException) {
                throw RuntimeException("If you want to use RTMP stream. Please implementation 'com.lmy.codec:rtmp:latestVersion'")
            }
        }

        fun connect(url: String, timeOut: Int): Int {
            return methodConnect.invoke(thiz, url, timeOut) as Int
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
    }
}