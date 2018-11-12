/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.presenter.impl

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioFormat
import android.media.MediaCodec
import android.view.TextureView
import com.lmy.codec.decoder.AudioDecoder
import com.lmy.codec.decoder.Decoder
import com.lmy.codec.decoder.VideoDecoder
import com.lmy.codec.decoder.VideoExtractor
import com.lmy.codec.decoder.impl.AudioDecoderImpl
import com.lmy.codec.decoder.impl.HardVideoDecoderImpl
import com.lmy.codec.egl.CameraEglSurface
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.media.AudioPlayer
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.presenter.VideoPlayer
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRenderImpl
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.util.debug_i
import java.nio.ByteBuffer

class VideoPlayerImpl(ctx: Context) : VideoPlayer, Decoder.OnSampleListener, Decoder.OnStateListener {

    override var onPlayStateListener: VideoPlayer.OnPlayStateListener? = null
    private var pipeline: Pipeline? = EventPipeline.create("VideoPlayImpl")
    private var eglSurface: CameraEglSurface? = null
    private var context: CodecContext = CodecContext(ctx)
    private var render: Render? = null
    private var videoDecoder: VideoDecoder? = null
    private var audioDecoder: AudioDecoder? = null
    private var player: AudioPlayer? = null
    private var extractor: VideoExtractor? = null
    private var view: TextureView? = null
    private var filter: BaseFilter? = null
    private var audioPts = 0L
    private var playing = false
    private var videoDurationUs = 0L

    override fun onSample(decoder: Decoder, info: MediaCodec.BufferInfo, data: ByteBuffer?) {
        if (decoder == audioDecoder) {
            audioPts = info.presentationTimeUs
            player?.play(data!!, info.size)
        } else if (decoder == this.videoDecoder) {
            if (audioPts > 0) {
//                debug_e("Delay: ${(info.presentationTimeUs - audioPts) / 1000}")
                videoDecoder?.delay(info.presentationTimeUs - audioPts)
            }
            onPlayStateListener?.onPlaying(this, info.presentationTimeUs, videoDurationUs)
            render?.onFrameAvailable()
        }
    }

    private fun check() {
        if (null == view) {
            throw RuntimeException("Please call setPreviewDisplay before call prepare.")
        }
    }

    private fun updateTexture() {
        eglSurface?.updateTexture()
        eglSurface?.updateLocation(context)
    }

    private fun prepareWrapper() {
        debug_i("prepareWrapper ${context.video.width}x${context.video.height}")
        eglSurface = CameraEglSurface(context.video.width, context.video.height)
        updateTexture()
    }

    private fun prepareExtractor() {
        extractor = VideoExtractor(context, context.ioContext.path!!)
        videoDurationUs = extractor!!.getVideoTrack()!!.getDurationUs()
    }

    private fun prepareDecoder() {
        videoDecoder = HardVideoDecoderImpl(context, extractor!!.getVideoTrack()!!, eglSurface!!.egl!!,
                eglSurface!!.surface!!, pipeline!!, true, this)
        videoDecoder?.prepare()
        if (extractor!!.containAudio()) {
            audioDecoder = AudioDecoderImpl(context, extractor!!.getAudioTrack()!!, true, this)
            audioDecoder?.prepare()
            player = AudioPlayer(audioDecoder!!.getSampleRate(), when (audioDecoder!!.getChannel()) {
                2 -> AudioFormat.CHANNEL_OUT_STEREO
                else -> AudioFormat.CHANNEL_OUT_MONO
            }, AudioFormat.ENCODING_PCM_16BIT)
        } else {
            debug_i("No audio track")
        }
    }

    private fun prepareRender(texture: SurfaceTexture, width: Int, height: Int) {
        prepareExtractor()
        prepareWrapper()
        prepareDecoder()
        videoDecoder!!.post(Runnable {
            render = DefaultRenderImpl(context, eglSurface!!, pipeline!!, filter)
            render?.start(texture, width, height)
            render?.updateSize(width, height)
            onPlayStateListener?.onPrepared(this, videoDurationUs)
        })
    }

    @Synchronized
    override fun seekTo(timeUs: Long) {
        pipeline?.queueEvent(Runnable {
            debug_i("seekTo $timeUs")
            extractor?.seekTo(timeUs)
        })
    }

    override fun reset() {
        playing = false
        player?.release()
        player = null
        render?.release()
        render = null
        pipeline?.queueEvent(Runnable {
            eglSurface?.release()
            eglSurface = null
            videoDecoder?.release()
            videoDecoder = null
            audioDecoder?.release()
            audioDecoder = null
            extractor?.release()
            extractor = null
            onPlayStateListener?.onStop(this)
        })
        context.reset()
    }

    override fun prepare() {
        check()
        if (this.view!!.isAvailable) {
            pipeline?.queueEvent(Runnable {
                prepareRender(this.view!!.surfaceTexture, this.view!!.width, this.view!!.height)
            }, true)
        } else {
            pipeline?.sleep()
            this.view!!.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {
                }

                override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
                }

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
                    return true
                }

                override fun onSurfaceTextureAvailable(texture: SurfaceTexture?, width: Int, height: Int) {
                    if (null != texture) {
                        pipeline?.queueEvent(Runnable {
                            prepareRender(texture, width, height)
                        }, true)
                        pipeline?.wake()
                    }
                }
            }
        }
    }

    override fun start() {
        pipeline?.queueEvent(Runnable {
            playing = true
            debug_i("start")
            onPlayStateListener?.onStart(this)
            videoDecoder?.start()
            audioDecoder?.start()
        })
    }

    override fun pause() {
        pipeline?.queueEvent(Runnable {
            playing = false
            debug_i("pause")
            onPlayStateListener?.onPause(this)
            videoDecoder?.pause()
            audioDecoder?.pause()
        })
    }

    override fun isPlaying(): Boolean = playing

    override fun stop() {
        pipeline?.queueEvent(Runnable {
            playing = false
            onPlayStateListener?.onStop(this)
            videoDecoder?.stop()
            audioDecoder?.stop()
        })
    }

    override fun setInputResource(path: String) {
        context.ioContext.path = path
    }

    override fun setPreviewDisplay(view: TextureView) {
        this.view = view
    }

    override fun release() {
        player?.release()
        player = null
        render?.release()
        render = null
        pipeline?.queueEvent(Runnable {
            eglSurface?.release()
            eglSurface = null
            videoDecoder?.release()
            videoDecoder = null
            audioDecoder?.release()
            audioDecoder = null
            extractor?.release()
            extractor = null
            onPlayStateListener?.onStop(this)
        })
        pipeline?.quit()
        pipeline = null
        context.release()
        view = null
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

    override fun onStart(decoder: Decoder) {

    }

    override fun onPause(decoder: Decoder) {
        playing = false
    }

    override fun onEnd(decoder: Decoder) {
        playing = false
    }
}