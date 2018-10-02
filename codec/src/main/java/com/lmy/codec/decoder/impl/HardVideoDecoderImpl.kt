package com.lmy.codec.decoder.impl

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.view.Surface
import com.lmy.codec.decoder.Decoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Track
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import com.lmy.codec.wrapper.CameraTextureWrapper
import java.io.IOException


class HardVideoDecoderImpl(val context: CodecContext,
                           private val pipeline: Pipeline,
                           private val forPlay: Boolean = false) : Decoder {
    override var onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener? = null
    override var textureWrapper: CameraTextureWrapper? = null
    private var extractor: MediaExtractor? = null
    private var videoTrack: Track? = null
    private var audioTrack: Track? = null
    private var codec: MediaCodec? = null
    private var path: String? = null
    private var videoInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private var startting = false
    private var eos = false
    private var lastPts = 0L

    override fun setInputResource(path: String) {
        this.path = path
    }

    override fun reset() {
        eos = false
        lastPts = 0
    }

    private fun prepareExtractor() {
        pipeline?.queueEvent(Runnable {
            extractor = MediaExtractor()
            try {
                extractor?.setDataSource(this.path)
            } catch (e: IOException) {
                debug_e("File($path) not found")
                return@Runnable
            }
            videoTrack = Track.getVideoTrack(extractor!!)
            context.video.width = getWidth()
            context.video.height = getHeight()
            context.cameraSize.width = context.video.width
            context.cameraSize.height = context.video.height
        })
    }

    private fun updateTexture() {
        textureWrapper?.updateTexture()
        textureWrapper?.updateLocation(context)
        textureWrapper?.surfaceTexture!!.setOnFrameAvailableListener(onFrameAvailableListener)
    }

    private fun prepareWrapper() {
        pipeline?.queueEvent(Runnable {
            debug_i("prepareWrapper ${getWidth()}x${getHeight()}")
            textureWrapper = CameraTextureWrapper(getWidth(), getHeight())
            updateTexture()
        })
    }

    override fun prepare() {
        prepareExtractor()
        prepareWrapper()
        pipeline?.queueEvent(Runnable {
            if (null == videoTrack) {
                debug_e("Video track not found")
                return@Runnable
            }
            videoTrack!!.select(extractor!!)
            try {
                codec = MediaCodec.createDecoderByType(videoTrack!!.format.getString(MediaFormat.KEY_MIME))
                codec!!.configure(videoTrack!!.format, Surface(textureWrapper!!.surfaceTexture), null, 0)
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
            val d = videoInfo.presentationTimeUs / 1000 - lastPts
            lastPts = videoInfo.presentationTimeUs / 1000
            d
        } else 0
        pipeline?.queueEvent(Runnable {
            if (!startting) return@Runnable
            textureWrapper?.egl?.makeCurrent()
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
                dequeue()
            } else {
                debug_e("Cannot get input buffer!")
            }
//            debug_i("next ${videoInfo.presentationTimeUs}")
            if (!eos) {
                next()
            }
        }, delay)
    }

    private fun dequeue() {
        val index = codec!!.dequeueOutputBuffer(videoInfo, WAIT_TIME)
        when (index) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> debug_i("INFO_OUTPUT_FORMAT_CHANGED")
            MediaCodec.INFO_TRY_AGAIN_LATER -> debug_i("INFO_TRY_AGAIN_LATER")
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> debug_i("INFO_OUTPUT_BUFFERS_CHANGED")
            else -> codec!!.releaseOutputBuffer(index, true)
        }
        if (videoInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
            debug_i("buffer stream end")
        }
    }

    override fun start() {
        if (eos) {
            debug_i("EOS!")
            return
        }
        startting = true
        next()
    }

    override fun pause() {
        if (eos) {
            debug_i("EOS!")
            return
        }
        startting = false
    }

    @Synchronized
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
    }

    override fun post(event: Runnable) {
        pipeline.queueEvent(event)
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