package com.lmy.codec.wrapper

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
        bufferSize = AudioRecord.getMinBufferSize(parameter.audio.sampleRateInHz,
                parameter.audio.channel, parameter.audio.pcm)
        debug_e("bufferSize: $bufferSize")
        buffer = ByteArray(bufferSize)
        record = AudioRecord(MediaRecorder.AudioSource.MIC, parameter.audio.sampleRateInHz,
                parameter.audio.channel, parameter.audio.pcm, bufferSize)
        record?.startRecording()
        thread = Thread(this)
        thread?.start()
    }

    private fun read() {
        val bufferReadResult = record!!.read(buffer, 0, bufferSize)
        onPCMListener?.onPCMSample(buffer!!)
    }

    override fun run() {
        synchronized(mStartSyn) {
            while (mStart) {
                read()
            }
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