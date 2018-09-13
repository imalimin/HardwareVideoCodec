/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.muxer.impl

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.encoder.impl.AudioEncoderImpl
import com.lmy.codec.entity.Sample
import com.lmy.codec.muxer.Muxer
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.util.debug_e
import java.io.File
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class MuxerImpl(var path: String,
                private var muxer: MediaMuxer? = null,
                var videoTrack: Int = 0,
                var audioTrack: Int = 0,
                private var mFrameCount: Int = 0,
                private var mVideoTrackReady: Boolean = false,
                private var mAudioTrackReady: Boolean = false,
                private var mStart: Boolean = false,
                override var onMuxerListener: Muxer.OnMuxerListener? = null) : Muxer {

    private val mQueue = LinkedList<Sample>()
    private val mWriteSyn = Any()
    private var mAudioPipeline = EventPipeline.create("AudioWritePipeline")
    private var mVideoPipeline = EventPipeline.create("VideoWritePipeline")

    init {
        start()
    }

    private fun start() {
        mFrameCount = 0
        mVideoTrackReady = false
        mAudioTrackReady = false
        mStart = false
        //删除已存在的文件
        val file = File(path)
        if (file.exists()) file.delete()
        muxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    override fun reset() {
        mVideoPipeline.queueEvent(Runnable {
            stop()
            start()
        })
    }

    override fun onFormatChanged(encoder: Encoder, format: MediaFormat) {
        if (encoder is AudioEncoderImpl) {
            debug_e("Add audio track")
            addAudioTrack(format)
        } else {
            addVideoTrack(format)
        }
    }

    /**
     * 编码后的帧数据
     * For VideoEncoderImpl
     */
    override fun onSample(encoder: Encoder, info: MediaCodec.BufferInfo, data: ByteBuffer) {
        if (encoder is AudioEncoderImpl) {
            writeAudioSample(Sample.wrap(info, data))
        } else {
            writeVideoSample(Sample.wrap(info, data))
        }
    }

    private fun ready() {
        if (mVideoTrackReady && mAudioTrackReady) {
            muxer?.start()
            mStart = true
            debug_e("Muxer start")
        }
    }

    override fun addVideoTrack(format: MediaFormat) {
        try {
            videoTrack = muxer!!.addTrack(format)
        } catch (e: Exception) {
            debug_e("Add video track failed")
            onMuxerListener?.onError(ERROR_ADD_TRACK, "Add video track failed")
            e.printStackTrace()
            return
        }
        mVideoTrackReady = true
        ready()
    }

    override fun addAudioTrack(format: MediaFormat) {
        try {
            audioTrack = muxer!!.addTrack(format)
        } catch (e: Exception) {
            debug_e("Add audio track failed")
            onMuxerListener?.onError(ERROR_ADD_TRACK, "Add audio track failed")
            e.printStackTrace()
            return
        }
        mAudioTrackReady = true
        ready()
    }

    override fun writeVideoSample(sample: Sample) {
        if (!mStart) return
        ++mFrameCount
        synchronized(mWriteSyn) {
            mQueue.push(sample)
        }
        mVideoPipeline.queueEvent(Runnable {
            synchronized(mWriteSyn) {
                while (!mQueue.isEmpty()) {
                    writeSample(videoTrack, mQueue.poll())
                }
            }
        })
    }

    override fun writeAudioSample(sample: Sample) {
        if (!mStart) return
        mAudioPipeline.queueEvent(Runnable {
            writeSample(audioTrack, sample)
        })
    }

    private fun writeSample(track: Int, sample: Sample) {
        try {
//            debug_e("write${if (videoTrack == track) "Video" else "Audio"}" +
//                    "Sample($mFrameCount, ${sample.bufferInfo.presentationTimeUs}): ${sample.bufferInfo.size}")
            muxer?.writeSampleData(track, sample.sample, sample.bufferInfo)
        } catch (e: Exception) {
            onMuxerListener?.onError(ERROR_WRITE, "Write sample failed")
            e.printStackTrace()
        }
    }

    override fun release() {
        debug_e("Muxer release")
        mVideoPipeline.quit()
        mAudioPipeline.quit()
        stop()
    }

    private fun stop() {
        mStart = false
        try {
            muxer?.release()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val ERROR_ADD_TRACK = 0x200
        const val ERROR_WRITE = 0x201
    }
}