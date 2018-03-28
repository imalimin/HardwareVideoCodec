package com.lmy.codec.impl

import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.Muxer
import com.lmy.codec.entity.Sample
import com.lmy.codec.util.debug_e

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class MuxerImpl(var format: MediaFormat,
                var path: String,
                private var muxer: MediaMuxer? = null,
                private var videoTrack: Int = 0) : Muxer {

    companion object {
        private val WRITE = 0x1
    }

    private var mHandlerThread = HandlerThread("Write_Thread")
    private var mHandler: Handler? = null

    init {
        muxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        videoTrack = muxer!!.addTrack(format)
        muxer?.start()
        initThread()
    }

    private fun initThread() {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    WRITE -> {
                        writeSample(msg.obj as Sample)
                    }
                }
            }
        }
    }

    override fun release() {
        muxer?.stop()
        muxer?.release()
    }

    override fun write(sample: Sample) {
        if (null == mHandler) return
        mHandler?.removeMessages(WRITE)
        mHandler?.sendMessage(mHandler!!.obtainMessage(WRITE, sample))
    }

    private fun writeSample(sample: Sample) {
        debug_e("writeSample")
        muxer?.writeSampleData(videoTrack, sample.sample, sample.bufferInfo)
    }
}