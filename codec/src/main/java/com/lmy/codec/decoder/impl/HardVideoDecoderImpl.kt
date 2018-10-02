package com.lmy.codec.decoder.impl

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.view.Surface
import com.lmy.codec.decoder.Decoder
import com.lmy.codec.entity.Track
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import java.io.IOException

class HardVideoDecoderImpl(val texture: SurfaceTexture) : Decoder {
    override var onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener? = null
    private var extractor: MediaExtractor? = null
    private var videoTrack: Track? = null
    private var audioTrack: Track? = null
    private var codec: MediaCodec? = null
    private var format: MediaFormat? = null
    private var path: String? = null
    private var pipeline: Pipeline? = EventPipeline.create("HVDecoderImpl")
    private var videoInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private var startting = false
    private var eos = false

    override fun setInputResource(path: String) {
        this.path = path
    }

    override fun reset() {
        eos = false
    }

    override fun prepare() {
        pipeline?.queueEvent(Runnable {
            extractor = MediaExtractor()
            try {
                extractor?.setDataSource(this.path)
            } catch (e: IOException) {
                debug_e("File($path) not found")
                return@Runnable
            }
            videoTrack = Track.getVideoTrack(extractor!!)
            if (null == videoTrack) {
                debug_e("Video track not found")
                return@Runnable
            }
            videoTrack!!.select(extractor!!)
            try {
                codec = MediaCodec.createDecoderByType(videoTrack!!.format.getString(MediaFormat.KEY_MIME))
                codec!!.configure(videoTrack!!.format, Surface(texture), null, 0)
            } catch (e: IOException) {
                debug_e("Cannot open decoder")
                return@Runnable
            }
            codec?.start()
        })
    }

    private fun decode() {
        pipeline?.queueEvent(Runnable {
            if (!startting) return@Runnable
            val index = codec!!.dequeueInputBuffer(WAIT_TIME)
            if (index >= 0) {
                val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    codec!!.getInputBuffer(index)
                } else {
                    codec!!.inputBuffers[index]
                }
                val size = extractor!!.readSampleData(buffer, 0)
                if (size < 0) {
                    codec!!.queueInputBuffer(index, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    eos = true
                    startting = false
                } else {
                    codec!!.queueInputBuffer(index, 0, size, extractor!!.sampleTime, 0)
                    extractor!!.advance()
                }
                debug_i("decode $size")
            } else {
                debug_e("Cannot get input buffer!")
            }
            decode()
        })
    }

    override fun start() {
        if (eos) {
            debug_i("EOS!")
            return
        }
        startting = true
        decode()
    }

    override fun pause() {
        if (eos) {
            debug_i("EOS!")
            return
        }
        startting = false
    }

    override fun stop() {
        pause()
        pipeline?.queueEvent(Runnable {
            extractor?.release()
            extractor = null
            codec?.stop()
            codec?.release()
            codec = null
        })
    }

    override fun release() {
        stop()
        pipeline?.quit()
    }

    override fun getWidth(): Int = if (null != videoTrack)
        videoTrack!!.format.getInteger(MediaFormat.KEY_WIDTH) else 0

    override fun getHeight(): Int = if (null != videoTrack)
        videoTrack!!.format.getInteger(MediaFormat.KEY_HEIGHT) else 0

    override fun getDuration(): Int = if (null != videoTrack)
        videoTrack!!.format.getInteger(MediaFormat.KEY_DURATION) / 1000 else 0

    companion object {
        private const val WAIT_TIME = 10000L
    }
}