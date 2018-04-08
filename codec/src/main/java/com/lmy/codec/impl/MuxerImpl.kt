package com.lmy.codec.impl

import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.Muxer
import com.lmy.codec.entity.Sample
import com.lmy.codec.util.debug_e
import java.io.File
import java.util.*

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class MuxerImpl(var path: String,
                private var muxer: MediaMuxer? = null,
                var videoTrack: Int = 0,
                var audioTrack: Int = 0) : Muxer {

    companion object {
        private val WRITE = 0x1
    }

    private val mQueue = LinkedList<Sample>()
    private val mWriteSyn = Any()
    private var mHandlerThread = HandlerThread("Write_Thread")
    private var mHandler: Handler? = null
    private var mAudioThread = HandlerThread("Write_Audio_Thread")
    private var mAudioHandler: Handler? = null
    private var mFrameCount = 0
    private var mVideoTrackReady = false
    private var mAudioTrackReady = false
    private var mStart = false

    init {
        //删除已存在的文件
        val file = File(path)
        if (file.exists()) file.delete()
        muxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        initThread()
    }

    private fun ready() {
        if (mVideoTrackReady) {
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
            e.printStackTrace()
            return
        }
        mAudioTrackReady = true
        ready()
    }

    private fun initThread() {
        mHandlerThread.start()
        mAudioThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    WRITE -> {
                        synchronized(mWriteSyn) {
                            while (!mQueue.isEmpty()) {
                                writeSample(videoTrack, mQueue.poll())
                            }
                        }
                    }
                }
            }
        }
        mAudioHandler = object : Handler(mAudioThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    WRITE -> {
                        writeSample(msg.arg1, msg.obj as Sample)
                    }
                }
            }
        }
    }

    override fun release() {
        debug_e("Muxer release")
        mHandlerThread.quitSafely()
        mAudioThread.quitSafely()
        if (mStart) {
            muxer?.stop()
        }
        muxer?.release()
    }

    override fun writeVideoSample(sample: Sample) {
        if (null == mHandler || !mStart) return
        ++mFrameCount
        synchronized(mWriteSyn) {
            mQueue.push(sample)
            mHandler?.sendMessage(mHandler!!.obtainMessage(WRITE, videoTrack, 0, sample))
        }
    }

    override fun writeAudioSample(sample: Sample) {
        if (null == mAudioHandler || !mStart) return
        mAudioHandler?.sendMessage(mAudioHandler!!.obtainMessage(WRITE, audioTrack, 0, sample))
    }

    private fun writeSample(track: Int, sample: Sample) {
        try {
            debug_e("write${if (videoTrack == track) "Video" else "Audio"}" +
                    "Sample($mFrameCount, ${sample.bufferInfo.presentationTimeUs}): ${sample.bufferInfo.size}")
            muxer?.writeSampleData(track, sample.sample, sample.bufferInfo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}