package com.lmy.codec.decoder.impl

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.view.Surface
import com.lmy.codec.decoder.VideoDecoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Track
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import com.lmy.codec.wrapper.CameraTextureWrapper
import java.io.IOException


class HardVideoDecoderImpl(val context: CodecContext,
                           private val track: Track,
                           private val textureWrapper: CameraTextureWrapper,
                           private val pipeline: Pipeline,
                           private val forPlay: Boolean = false) : VideoDecoder {
    private var codec: MediaCodec? = null
    private var bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private var starting = false
    private var eos = false
    private var lastPts = 0L

    override fun reset() {
        eos = false
        lastPts = 0
    }

    override fun prepare() {
        pipeline?.queueEvent(Runnable {
            try {
                codec = MediaCodec.createDecoderByType(track.format.getString(MediaFormat.KEY_MIME))
                codec!!.configure(track.format, Surface(textureWrapper.surfaceTexture), null, 0)
            } catch (e: IOException) {
                debug_e("Cannot open decoder")
                return@Runnable
            }
            codec?.start()
        })
    }

    @Synchronized
    private fun next() {
        val delay = if (forPlay) {
            val d = bufferInfo.presentationTimeUs / 1000 - lastPts
            lastPts = bufferInfo.presentationTimeUs / 1000
            d
        } else 0
        pipeline?.queueEvent(Runnable {
            synchronized(this@HardVideoDecoderImpl) {
                if (!starting) return@Runnable
                val ttt = System.currentTimeMillis()
                textureWrapper.egl?.makeCurrent()
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
                            eos = true
                            starting = false
                        } else {
                            codec!!.queueInputBuffer(index, 0, size, track.extractor.sampleTime, 0)
                            track.extractor.advance()
                        }
//                        track.unselect()
                    }
                    dequeue()
                } else {
                    debug_e("Cannot get input buffer!")
                }
                lastPts += System.currentTimeMillis() - ttt
//            debug_i("next ${videoInfo.presentationTimeUs}")
                if (!eos) {
                    next()
                }
            }
        }, delay)
    }

    private fun dequeue() {
        val index = codec!!.dequeueOutputBuffer(bufferInfo, WAIT_TIME)
        when (index) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> debug_i("INFO_OUTPUT_FORMAT_CHANGED")
            MediaCodec.INFO_TRY_AGAIN_LATER -> debug_i("INFO_TRY_AGAIN_LATER")
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> debug_i("INFO_OUTPUT_BUFFERS_CHANGED")
            else -> codec!!.releaseOutputBuffer(index, true)
        }
        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
            debug_i("buffer stream end")
        }
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

    @Synchronized
    override fun stop() {
        pause()
        pipeline?.queueEvent(Runnable {
            codec?.stop()
            codec?.release()
            codec = null
        })
    }

    override fun release() {
        stop()
    }

    override fun post(event: Runnable) {
        pipeline.queueEvent(event)
    }

    override fun getWidth(): Int = if (null != track)
        track!!.format.getInteger(MediaFormat.KEY_WIDTH) else 0

    override fun getHeight(): Int = if (null != track)
        track!!.format.getInteger(MediaFormat.KEY_HEIGHT) else 0

    override fun getDuration(): Int = if (null != track)
        track!!.format.getInteger(MediaFormat.KEY_DURATION) / 1000 else 0

    companion object {
        private const val WAIT_TIME = 10000L
    }
}