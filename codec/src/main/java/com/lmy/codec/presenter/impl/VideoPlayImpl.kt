package com.lmy.codec.presenter.impl

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaExtractor
import android.media.MediaFormat
import android.view.TextureView
import com.lmy.codec.decoder.Decoder
import com.lmy.codec.decoder.impl.AudioDecoderImpl
import com.lmy.codec.decoder.impl.HardVideoDecoderImpl
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Track
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.presenter.VideoPlay
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRenderImpl
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import com.lmy.codec.wrapper.CameraTextureWrapper
import java.io.IOException

class VideoPlayImpl(ctx: Context) : VideoPlay, SurfaceTexture.OnFrameAvailableListener {
    private var pipeline: Pipeline? = EventPipeline.create("VideoPlayImpl")
    private var textureWrapper: CameraTextureWrapper? = null
    private var context: CodecContext = CodecContext(ctx)
    private var render: Render? = null
    private var decoder: Decoder? = null
    private var audioDecoder: Decoder? = null
    private var extractor: MediaExtractor? = null
    private var videoTrack: Track? = null
    private var audioTrack: Track? = null
    private var view: TextureView? = null
    private var filter: BaseFilter? = null

    private fun check() {
        if (null == view) {
            throw RuntimeException("Please call setPreviewDisplay before call prepare.")
        }
    }

    private fun updateTexture() {
        textureWrapper?.updateTexture()
        textureWrapper?.updateLocation(context)
        textureWrapper?.surfaceTexture!!.setOnFrameAvailableListener(this)
    }

    private fun prepareWrapper() {
        debug_i("prepareWrapper ${context.video.width}x${context.video.height}")
        textureWrapper = CameraTextureWrapper(context.video.width, context.video.height)
        updateTexture()
    }

    private fun prepareExtractor() {
        extractor = MediaExtractor()
        try {
            extractor?.setDataSource(context.ioContext.path)
        } catch (e: IOException) {
            debug_e("File(${context.ioContext.path}) not found")
            return
        }
        videoTrack = Track.getVideoTrack(extractor!!)
        audioTrack = Track.getAudioTrack(extractor!!)
        context.orientation = if (videoTrack!!.format.containsKey(KEY_ROTATION))
            videoTrack!!.format.getInteger(KEY_ROTATION) else 0
        if (context.isHorizontal()) {
            context.video.width = videoTrack!!.format.getInteger(MediaFormat.KEY_WIDTH)
            context.video.height = videoTrack!!.format.getInteger(MediaFormat.KEY_HEIGHT)
            context.cameraSize.width = context.video.width
            context.cameraSize.height = context.video.height
        } else {
            context.video.width = videoTrack!!.format.getInteger(MediaFormat.KEY_HEIGHT)
            context.video.height = videoTrack!!.format.getInteger(MediaFormat.KEY_WIDTH)
            context.cameraSize.width = context.video.height
            context.cameraSize.height = context.video.width
        }
    }

    private fun prepareDecoder() {
        decoder = HardVideoDecoderImpl(context, videoTrack!!, textureWrapper!!, pipeline!!, true)
        decoder?.prepare()
        if (null != audioTrack) {
            audioDecoder = AudioDecoderImpl(context, audioTrack!!, true)
            audioDecoder?.prepare()
        } else {
            debug_i("No audio track")
        }
    }

    private fun prepareRender(texture: SurfaceTexture, width: Int, height: Int) {
        prepareExtractor()
        prepareWrapper()
        prepareDecoder()
        decoder!!.post(Runnable {
            render = DefaultRenderImpl(context, textureWrapper!!, pipeline!!, filter)
            render?.start(texture, width, height)
            render?.updateSize(width, height)
        })
    }

    override fun reset() {

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

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        render?.onFrameAvailable()
    }

    override fun start() {
        pipeline?.queueEvent(Runnable {
            decoder?.start()
            audioDecoder?.start()
        })
    }

    override fun pause() {
        pipeline?.queueEvent(Runnable {
            decoder?.pause()
            audioDecoder?.pause()
        })
    }

    override fun stop() {
        pipeline?.queueEvent(Runnable {
            decoder?.stop()
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
        render?.release()
        render = null
        pipeline?.queueEvent(Runnable {
            textureWrapper?.release()
            textureWrapper = null
            decoder?.release()
            decoder = null
            audioDecoder?.release()
            audioDecoder = null
            extractor?.release()
            extractor = null
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

    companion object {
        private const val KEY_ROTATION = "rotation-degrees"
    }
}