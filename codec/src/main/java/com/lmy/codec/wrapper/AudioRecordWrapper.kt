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
        buffer = ByteArray(getBufferSize())

        val minBufferSize = AudioRecord.getMinBufferSize(parameter.audio.sampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO, parameter.audio.pcm)
        val bufferSize = Math.max(BUFFER_SIZE_FACTOR * minBufferSize, buffer!!.size)

        debug_e("bufferSize: $bufferSize, buffer`s size: ${buffer!!.size}")
        record = AudioRecord(MediaRecorder.AudioSource.MIC, parameter.audio.sampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO, parameter.audio.pcm, bufferSize)
        record?.startRecording()
        thread = Thread(this)
        thread?.start()
    }

    private fun read() {
        val bufferReadResult = record!!.read(buffer, 0, buffer!!.size)
        if (bufferReadResult > 0) {
            onPCMListener?.onPCMSample(buffer!!)
        }
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


    private fun getBufferSize(): Int {
        val bytesPerFrame = parameter.audio.channel * (BITS_PER_SAMPLE / 8)
        return bytesPerFrame * parameter.audio.sampleRateInHz / BUFFERS_PER_SECOND
    }

    fun setOnPCMListener(listener: OnPCMListener) {
        onPCMListener = listener
    }

    interface OnPCMListener {
        fun onPCMSample(buffer: ByteArray)
    }

    companion object {
        // Default audio data format is PCM 16 bit per sample.
        // Guaranteed to be supported by all devices.
        val BITS_PER_SAMPLE = 16
        // Requested size of each recorded buffer provided to the client.
        val CALLBACK_BUFFER_SIZE_MS = 10
        // Average number of callbacks per second.
        val BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS
        // We ask for a native buffer size of BUFFER_SIZE_FACTOR * (minimum required
        // buffer size). The extra space is allocated to guard against glitches under
        // high load.
        val BUFFER_SIZE_FACTOR = 2
    }
}