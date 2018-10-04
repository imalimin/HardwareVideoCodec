package com.lmy.codec.decoder.impl

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import com.lmy.codec.decoder.AudioDecoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Track
import com.lmy.codec.media.AudioPlayer
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import java.io.IOException

class AudioDecoderImpl(val context: CodecContext,
                       private val track: Track,
                       private val forPlay: Boolean = false,
                       override val onSampleListener: AudioDecoder.OnSampleListener? = null) : AudioDecoder {

    private var pipeline: Pipeline? = EventPipeline.create("AudioDecoderPipeline")
    private var mDequeuePipeline: Pipeline? = EventPipeline.create("AudioDequeuePipeline")
    private var codec: MediaCodec? = null
    private var bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private var starting = false
    private var eos = false
    private var lastPts = 0L
    private var player: AudioPlayer? = null
    private var dequeueLoop = true

    override fun reset() {
        eos = false
        lastPts = 0
    }

    override fun prepare() {
        pipeline?.queueEvent(Runnable {
            player = AudioPlayer(getSampleRateInHz(), when (getChannel()) {
                2 -> AudioFormat.CHANNEL_OUT_STEREO
                else -> AudioFormat.CHANNEL_OUT_MONO
            }, AudioFormat.ENCODING_PCM_16BIT)
            try {
                codec = MediaCodec.createDecoderByType(track.format.getString(MediaFormat.KEY_MIME))
                codec!!.configure(track.format, null, null, 0)
            } catch (e: IOException) {
                debug_e("Cannot open decoder")
                return@Runnable
            }
            codec?.start()
        })
    }

    private fun dequeue() {
        while (true) {
            val index = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
            when (index) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
//                    debug_i("INFO_OUTPUT_FORMAT_CHANGED")
                    return
                }
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
//                    debug_i("INFO_TRY_AGAIN_LATER")
                    return
                }
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
//                    debug_i("INFO_OUTPUT_BUFFERS_CHANGED")
                    return
                }
                else -> {
                    if (index >= 0) {
                        val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            codec!!.getOutputBuffer(index)
                        } else {
                            codec!!.outputBuffers[index]
                        }
                        player?.play(buffer, bufferInfo.size)
                        onSampleListener?.onSample(this@AudioDecoderImpl, bufferInfo, buffer)
                        buffer.clear()
                        codec!!.releaseOutputBuffer(index, false)
                    } else {
                        return
                    }
                }
            }
            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                debug_i("buffer stream end")
            }
        }
    }

    private fun next() {
        val delay = if (forPlay) {
            val d = bufferInfo.presentationTimeUs / 1000 - lastPts
            lastPts = bufferInfo.presentationTimeUs / 1000
            d
        } else 0
        pipeline?.queueEvent(Runnable {
            synchronized(this@AudioDecoderImpl) {
                if (!starting) return@Runnable
                val index = codec!!.dequeueInputBuffer(WAIT_TIME)
                if (index >= 0) {
                    val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        codec!!.getInputBuffer(index)
                    } else {
                        codec!!.inputBuffers[index]
                    }
                    synchronized(track.extractor) {
                        track.select()
                        val size = track.extractor.readSampleData(buffer, 0)
                        if (size < 0) {
                            codec!!.queueInputBuffer(index, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            debug_e("eos!")
                            eos = true
                            starting = false
                        } else {
                            codec!!.queueInputBuffer(index, 0, size, track.extractor.sampleTime, 0)
                            track.extractor.advance()
                        }
                        track.unselect()
                    }
                    dequeue()
                } else {
                    debug_e("Cannot get input buffer!")
                }
//            debug_i("next ${videoInfo.presentationTimeUs}")
                if (!eos) {
                    next()
                }
            }
        }, delay)

    }

    override fun start() {
        if (eos) {
            debug_i("EOS!")
            return
        }
        starting = true
        next()
    }

    override fun pause() {
        if (eos) {
            debug_i("EOS!")
            return
        }
        starting = false
    }

    override fun stop() {
        pause()
        pipeline?.queueEvent(Runnable {
            player?.release()
            player = null
            codec?.stop()
            codec?.release()
            codec = null
        })
    }

    override fun release() {
        stop()
        pipeline?.quit()
        pipeline = null
        mDequeuePipeline?.quit()
        mDequeuePipeline = null
    }

    override fun getDuration(): Int = track.format.getInteger(MediaFormat.KEY_DURATION) / 1000

    override fun post(event: Runnable) {
        pipeline?.queueEvent(event)
    }

    override fun getSampleRateInHz(): Int = track.format.getInteger(MediaFormat.KEY_SAMPLE_RATE)

    override fun getChannel(): Int = track.format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

    companion object {
        private const val WAIT_TIME = 10000L
    }
}