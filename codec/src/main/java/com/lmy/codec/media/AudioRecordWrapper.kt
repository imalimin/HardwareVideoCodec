/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.media

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.native.processor.DeNoise
import com.lmy.codec.util.debug_e
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.Short.reverseBytes


/**
 * Created by 李明艺 on 2018/3/31.
 * Project Name：HardwareVideoCodec.
 * @author lrlmy@foxmail.com
 */
class AudioRecordWrapper(var context: CodecContext,
                         private var bufferSize: Int = 0,
                         private var record: AudioRecord? = null,
                         private var thread: Thread? = null,
                         private var buffer: ByteArray? = null,
                         private var mStart: Boolean = true,
                         private var mDeNoise: DeNoise? = null) : Runnable {
    private val mStartSyn = Any()
    private var onPCMListener: OnPCMListener? = null
    private lateinit var dos: RandomAccessFile
    private var pcmSize = 0

    init {
//        initPcmFile()
        buffer = ByteArray(getBufferSize())

        val minBufferSize = AudioRecord.getMinBufferSize(context.audio.sampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO, context.audio.sampleBits)
        val bufferSize = Math.max(BUFFER_SIZE_FACTOR * minBufferSize, buffer!!.size)

        debug_e("bufferSize: $bufferSize, buffer`s size: ${buffer!!.size}")
        record = AudioRecord(MediaRecorder.AudioSource.MIC, context.audio.sampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO, context.audio.sampleBits, bufferSize)
        if (AudioRecord.STATE_INITIALIZED != record!!.state) {
            debug_e("AudioRecord initialize failed!")
        }
        if (context.audio.deNoise) {
            mDeNoise = DeNoise(context.audio.sampleRateInHz, buffer!!.size / 2)
        }
        thread = Thread(this)
        thread?.name = "AudioRecorder"
        thread?.start()
    }

    private fun initPcmFile() {
        val pcmFile = File(Environment.getExternalStorageDirectory().absolutePath + "/test.pcm")
        if (pcmFile.exists())
            pcmFile.delete()
        dos = RandomAccessFile(pcmFile, "rw")
        dos.setLength(0)
//        writeWavHeader()
    }

    private fun write(data: ByteArray) {
        dos.write(data, 0, data.size)
    }

    private fun read() {
        val size = record!!.read(buffer, 0, buffer!!.size)
        if (size > 0) {
            pcmSize += size
//            val time = System.currentTimeMillis()
            mDeNoise?.preprocess(buffer!!)
//            debug_e("De noise: ${System.currentTimeMillis() - time}")
//            debug_e("pcmSize: $pcmSize, size: $size")
//            write(buffer!!)
            onPCMListener?.onPCMSample(buffer!!)
        }
    }

    override fun run() {
        pcmSize = 0
        record?.startRecording()
        while (mStart) {
            read()
        }
//        dos.seek(4)
//        dos.write(Integer.reverseBytes(44 + pcmSize))
//        dos.seek(40)
//        dos.write(Integer.reverseBytes(pcmSize))
//        dos.close()
    }

    fun stop() {
        synchronized(mStartSyn) {
            mStart = false
            record?.stop()
            record?.release()
            mDeNoise?.stop()
        }
    }


    fun getBufferSize(): Int {
        val bytesPerFrame = context.audio.channel * (getSampleBits() / 8)
        return bytesPerFrame * context.audio.sampleRateInHz / BUFFERS_PER_SECOND
    }

    private fun getSampleBits(): Int {
        return when (context.audio.sampleBits) {
            AudioFormat.ENCODING_PCM_16BIT -> 16
            AudioFormat.ENCODING_PCM_8BIT -> 8
            else -> 8
        }
    }

    fun setOnPCMListener(listener: OnPCMListener) {
        onPCMListener = listener
    }

    interface OnPCMListener {
        fun onPCMSample(buffer: ByteArray)
    }

    private fun writeWavHeader() {
        try {
            dos.writeBytes("RIFF")
            dos.writeInt(0)
            dos.writeBytes("WAVE")
            dos.writeBytes("fmt ")
            dos.writeInt(Integer.reverseBytes(16)) // Sub-chunk size, 16 for PCM
            dos.writeShort(reverseBytes(1).toInt()) // AudioFormat, 1 for PCM
            dos.writeShort(reverseBytes(context.audio.channel.toShort()).toInt())// Number of channels, 1 for mono, 2 for stereo
            dos.writeInt(Integer.reverseBytes(context.audio.sampleRateInHz)) // Sample rate
            dos.writeInt(Integer.reverseBytes(context.audio.sampleRateInHz * 16 * context.audio.channel / 8)) // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
            dos.writeShort(reverseBytes((context.audio.channel * 16 / 8).toShort()).toInt()) // Block align, NumberOfChannels*BitsPerSample/8
            dos.writeShort(reverseBytes(16).toInt()) // Bits per sample
            dos.writeBytes("data")
            dos.writeInt(0) // Data chunk size not known yet, write 0
        } catch (e: IOException) {
            e.printStackTrace()
        }
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