/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.presenter.impl

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.text.TextUtils
import android.view.TextureView
import com.lmy.codec.decoder.AudioDecoder
import com.lmy.codec.decoder.Decoder
import com.lmy.codec.decoder.VideoDecoder
import com.lmy.codec.decoder.VideoExtractor
import com.lmy.codec.decoder.impl.AudioDecoderImpl
import com.lmy.codec.decoder.impl.HardVideoDecoderImpl
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.encoder.impl.AudioEncoderImpl
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.helper.MuxerFactory
import com.lmy.codec.muxer.Muxer
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.pipeline.impl.GLEventPipeline
import com.lmy.codec.presenter.VideoProcessor
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRenderImpl
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import com.lmy.codec.wrapper.CameraTextureWrapper
import java.io.File
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/10/8.
 */
class VideoProcessorImpl private constructor(ctx: Context) : VideoProcessor, Decoder.OnSampleListener,
        Decoder.OnStateListener, Encoder.OnPreparedListener {

    private val context: CodecContext = CodecContext(ctx)
    private var filter: BaseFilter? = null
    private var pipeline: Pipeline? = EventPipeline.create("ImageProcessor")
    private var textureWrapper: CameraTextureWrapper? = null
    private var render: Render? = null
    private var extractor: VideoExtractor? = null
    private var videoDecoder: VideoDecoder? = null
    private var audioDecoder: AudioDecoder? = null
    private var videoEncoder: Encoder? = null
    private var audioEncoder: Encoder? = null
    private var muxer: Muxer? = null
    private var inputPath: String? = null
    private var flag: BooleanArray = booleanArrayOf(false, false)
    private var audioSample: ByteArray? = null
    private var endEvent: Runnable? = null
    private var startVideoPts = 0L
    private var frameCount = 0

    override fun onSample(decoder: Decoder, info: MediaCodec.BufferInfo, data: ByteBuffer?) {
        if (decoder == audioDecoder) {
//            debug_i("Write data size ${info.size}")
//            muxer?.writeAudioSample(Sample.wrap(info, data!!))
            debug_i("Write audio ${info.presentationTimeUs}")
            if (null != data) {
                data.get(audioSample)
                data.rewind()
                (audioEncoder as AudioEncoderImpl).onPCMSample(audioSample!!)
            }
        } else if (decoder == this.videoDecoder) {
            ++frameCount
            debug_e("Write video ${info.presentationTimeUs}, count=$frameCount")
            render?.onFrameAvailable()
            if (startVideoPts <= 0 && 0L != info.presentationTimeUs
                    && extractor!!.getVideoTrack()!!.getStartTime() != info.presentationTimeUs) {
                startVideoPts = info.presentationTimeUs
            }
            videoEncoder?.setPresentationTime(info.presentationTimeUs - startVideoPts)
            videoEncoder?.onFrameAvailable(null)
        }
    }

    override fun reset() {
        stop()
    }

    override fun onPrepared(encoder: Encoder) {
        pipeline?.queueEvent(Runnable {
            videoEncoder?.start()
            audioEncoder?.start()
            videoDecoder?.start()
        })
    }

    private fun prepareAudioEncoder() {
        if (null == audioDecoder) return
        context.audio.mime = extractor!!.getAudioTrack()!!.format.getString(MediaFormat.KEY_MIME)
        context.audio.channel = audioDecoder!!.getChannel()
        context.audio.sampleRateInHz = audioDecoder!!.getSampleRate()
        if (extractor!!.getAudioTrack()!!.format.containsKey(MediaFormat.KEY_BIT_RATE))
            context.audio.bitrate = extractor!!.getAudioTrack()!!
                    .format.getInteger(MediaFormat.KEY_BIT_RATE)
        if (extractor!!.getAudioTrack()!!.format.containsKey(MediaFormat.KEY_AAC_PROFILE))
            context.audio.profile = extractor!!.getAudioTrack()!!
                    .format.getInteger(MediaFormat.KEY_AAC_PROFILE)
        audioSample = ByteArray(audioDecoder!!.getSampleSize())
        debug_i("audioSample=${audioSample!!.size}")
        audioEncoder = AudioEncoderImpl.fromArray(context, audioSample!!.size)
        if (null != muxer) {
            muxer!!.addAudioTrack(audioEncoder!!.getOutputFormat())
            audioEncoder?.setOnSampleListener(muxer!!)
        }
    }

    private fun prepareVideoEncoder() {
        context.video.width = videoDecoder!!.getWidth()
        context.video.height = videoDecoder!!.getHeight()
        if (extractor!!.getVideoTrack()!!.format.containsKey(MediaFormat.KEY_FRAME_RATE))
            context.video.fps = extractor!!.getVideoTrack()!!
                    .format.getInteger(MediaFormat.KEY_FRAME_RATE)
        if (extractor!!.getVideoTrack()!!.format.containsKey(MediaFormat.KEY_I_FRAME_INTERVAL))
            context.video.iFrameInterval = extractor!!.getVideoTrack()!!
                    .format.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL)
        else {
            context.video.iFrameInterval = 5
            debug_i("Set iFrameInterval=${context.video.iFrameInterval}")
        }
        if (extractor!!.getVideoTrack()!!.format.containsKey(MediaFormat.KEY_BIT_RATE))
            context.video.bitrate = extractor!!.getVideoTrack()!!
                    .format.getInteger(MediaFormat.KEY_BIT_RATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && extractor!!.getVideoTrack()!!.format.containsKey(MediaFormat.KEY_PROFILE)) {
            context.video.profile = extractor!!.getVideoTrack()!!
                    .format.getInteger(MediaFormat.KEY_PROFILE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && extractor!!.getVideoTrack()!!.format.containsKey(MediaFormat.KEY_LEVEL)) {
            context.video.level = extractor!!.getVideoTrack()!!
                    .format.getInteger(MediaFormat.KEY_LEVEL)
        }
        videoEncoder = Encoder.Builder(context, render!!.getFrameBufferTexture(),
                textureWrapper!!.egl!!.eglContext!!)
                .setOnPreparedListener(this)
                .build()
        if (null != muxer) {
            videoEncoder?.setOnSampleListener(muxer!!)
        }
    }

    private fun prepareMuxer() {
        if (null == muxer) {
            muxer = MuxerFactory.getMuxer(context)
            muxer?.onMuxerListener = object : Muxer.OnMuxerListener {
                override fun onStart() {
                    audioDecoder?.start()
                }

                override fun onError(error: Int, msg: String) {
                    debug_e("Muxer error $error, $msg")
                }
            }
        } else {
            muxer?.reset()
        }
    }

    private fun updateTexture() {
        textureWrapper?.updateTexture()
        textureWrapper?.updateLocation(context)
    }

    private fun prepareWrapper() {
        debug_i("prepareWrapper ${context.video.width}x${context.video.height}")
        textureWrapper = CameraTextureWrapper(context.video.width, context.video.height)
        updateTexture()
    }

    private fun prepareDecoder() {
        videoDecoder = HardVideoDecoderImpl(context, extractor!!.getVideoTrack()!!, textureWrapper!!.egl!!,
                textureWrapper!!.surfaceTexture!!, pipeline!!, false, this)
        videoDecoder?.onStateListener = this
        videoDecoder?.prepare()
        if (extractor!!.containAudio()) {
            audioDecoder = AudioDecoderImpl(context, extractor!!.getAudioTrack()!!, false, this)
            audioDecoder?.onStateListener = this
            audioDecoder?.prepare()
        } else {
            debug_i("No audio track")
        }
    }

    override fun prepare() {
        pipeline?.queueEvent(Runnable {
            if (TextUtils.isEmpty(this.inputPath)) {
                throw IllegalStateException("Please prepared call setInputResource  before")
            }
            extractor = VideoExtractor(context, this.inputPath!!)
            prepareWrapper()
            render = DefaultRenderImpl(context, textureWrapper!!, pipeline!!, filter)
            render?.start(null, getWidth(), getHeight())
            render?.updateSize(getWidth(), getHeight())
        })
        pipeline?.queueEvent(Runnable {
            prepareDecoder()
        })
    }

    override fun setInputResource(file: File) {
        if (!file.exists()) {
            debug_e("Input file is not exists")
            return
        }
        this.inputPath = file.absolutePath
    }

    override fun setPreviewDisplay(view: TextureView) {

    }

    override fun save(path: String, end: Runnable?) {
        save(path, 0, 0, end)
    }

    override fun save(path: String, startMs: Int, endMs: Int, end: Runnable?) {
        context.ioContext.path = path
        if (startMs > 0 && endMs > 0) {
            pipeline?.queueEvent(Runnable {
                debug_i("-----> range")
                extractor!!.range(startMs * 1000L, endMs * 1000L)
            })
        }
        pipeline?.queueEvent(Runnable {
            if (null == extractor) {
                throw IllegalStateException("Please prepared processor before")
            }
            prepareMuxer()
            prepareAudioEncoder()
            prepareVideoEncoder()
            this.endEvent = end
        })
    }

    private fun stop() {
        startVideoPts = 0
        muxer?.release()
        muxer = null
        render?.release()
        render = null
        pipeline?.queueEvent(Runnable {
            textureWrapper?.release()
            textureWrapper = null
            videoDecoder?.release()
            videoDecoder = null
            audioDecoder?.release()
            audioDecoder = null
            extractor?.release()
            extractor = null
            videoEncoder?.stop()
            videoEncoder = null
            audioEncoder?.stop()
            audioEncoder = null
            audioSample = null
            GLEventPipeline.INSTANCE.quit()
            endEvent?.run()
        })
    }

    override fun release() {
        stop()
        pipeline?.quit()
        pipeline = null
        context.release()
    }

    override fun setFilter(filter: BaseFilter) {
        if (null == render) {
            this.filter = filter
        } else {
            this.filter = null
            render!!.setFilter(filter)
        }
    }

    override fun getFilter(): BaseFilter? {
        return if (null == render) {
            this.filter
        } else {
            render!!.getFilter()
        }
    }

    private fun getWidth(): Int = if (null == extractor) 0
    else extractor!!.getVideoTrack()!!.format.getInteger(MediaFormat.KEY_WIDTH)

    private fun getHeight(): Int = if (null == extractor) 0
    else extractor!!.getVideoTrack()!!.format.getInteger(MediaFormat.KEY_HEIGHT)

    override fun onStart(decoder: Decoder) {

    }

    override fun onPause(decoder: Decoder) {

    }

    override fun onEnd(decoder: Decoder) {
        if (decoder == videoDecoder) {
            flag[0] = true
        } else if (decoder == audioDecoder) {
            flag[1] = true
        }
        if (flag[0] && flag[1]) {
            muxer?.release()
            muxer = null
            endEvent?.run()
            endEvent = null
        }
    }

    companion object {
        fun create(ctx: Context): VideoProcessor = VideoProcessorImpl(ctx)
    }
}