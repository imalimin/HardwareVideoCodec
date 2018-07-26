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
import com.lmy.rtmp.RtmpClient
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/7/25.
 */
class RtmpMuxerImpl(var context: CodecContext,
                    private var client: RtmpClient = RtmpClient.build()) : Muxer {

    private var mAudioPipeline = EventPipeline.create("LivePipeline")

    init {
        start()
    }

    private fun start() {
        mAudioPipeline.queueEvent(Runnable {
            var ret = client.connect(context.ioContext.path!!, 10000)
            debug_i("RTMP connect: $ret")
            ret = client.connectStream(context.video.width, context.video.height)
            debug_i("RTMP connect stream: $ret")
        })
    }

    override fun reset() {
        mAudioPipeline.queueEvent(Runnable {
            debug_i("RTMP reset")
            val ret = client.connectStream(context.video.width, context.video.height)
            debug_i("RTMP connect stream: $ret")
        })
    }

    //分发MediaFormat
    override fun onFormatChanged(encoder: Encoder, format: MediaFormat) {
        if (encoder is AudioEncoderImpl) {
            debug_e("Add audio track")
            addAudioTrack(format)
        } else {
            debug_e("Add video track")
            addVideoTrack(format)
        }
    }

    //分发sample
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
        mAudioPipeline.queueEvent(Runnable {
            client.sendVideoSpecificData(sps, sps.size, pps, pps.size)
        })
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
        mAudioPipeline.queueEvent(Runnable {
            client.sendAudioSpecificData(esds, esds.size)
        })
    }

    override fun writeVideoSample(sample: Sample) {
        val data = ByteArray(sample.bufferInfo.size)
        sample.sample.rewind()
        sample.sample.get(data)
        sample.sample.rewind()
        mAudioPipeline.queueEvent(Runnable {
            client.sendVideo(data, data.size, sample.bufferInfo.presentationTimeUs / 1000)
        })
    }

    override fun writeAudioSample(sample: Sample) {
        val data = ByteArray(sample.bufferInfo.size)
        sample.sample.rewind()
        sample.sample.get(data)
        sample.sample.rewind()
        mAudioPipeline.queueEvent(Runnable {
            client.sendAudio(data, data.size, sample.bufferInfo.presentationTimeUs / 1000)
        })
    }

    override fun release() {
        mAudioPipeline.quit()
        client.stop()
    }
}