/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.wrapper

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.util.debug_e


/**
 * Created by 李明艺 on 2018/3/31.
 * Project Name：HardwareVideoCodec.
 * @author lrlmy@foxmail.com
 */
class AudioRecordWrapper(var parameter: Parameter,
                         private var bufferSize: Int = 0,
                         private var record: AudioRecord? = null,
                         private var thread: Thread? = null,
                         private var buffer: ByteArray? = null) : Runnable {
    private val mStartSyn = Any()
    private var mStart = true
    private var onPCMListener: OnPCMListener? = null

    init {
        val minBufferSize = AudioRecord.getMinBufferSize(parameter.audio.sampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO, parameter.audio.pcm)
        bufferSize = parameter.audio.samplePerFrame * parameter.video.fps
        if (bufferSize < minBufferSize)
            bufferSize = (minBufferSize / parameter.audio.samplePerFrame + 1) * parameter.audio.samplePerFrame * 2

        debug_e("bufferSize: $bufferSize")
        buffer = ByteArray(parameter.audio.samplePerFrame)
        record = AudioRecord(MediaRecorder.AudioSource.MIC, parameter.audio.sampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO, parameter.audio.pcm, bufferSize)
        record?.startRecording()
        thread = Thread(this)
        thread?.start()
    }

    private fun read() {
        val bufferReadResult = record!!.read(buffer, 0, parameter.audio.samplePerFrame)
        onPCMListener?.onPCMSample(buffer!!)
    }

    override fun run() {
        while (mStart) {
            read()
        }
    }

    fun stop() {
        synchronized(mStartSyn) {
            mStart = false
            record?.stop()
            record?.release()
        }
    }

    fun setOnPCMListener(listener: OnPCMListener) {
        onPCMListener = listener
    }

    interface OnPCMListener {
        fun onPCMSample(buffer: ByteArray)
    }
}