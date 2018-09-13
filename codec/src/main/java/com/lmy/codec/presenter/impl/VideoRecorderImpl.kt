package com.lmy.codec.presenter.impl

import android.content.Context
import android.graphics.SurfaceTexture
import android.text.TextUtils
import android.view.TextureView
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.encoder.impl.AudioEncoderImpl
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.helper.CodecFactory
import com.lmy.codec.helper.MuxerFactory
import com.lmy.codec.muxer.Muxer
import com.lmy.codec.pipeline.GLEventPipeline
import com.lmy.codec.presenter.VideoRecorder
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRenderImpl
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.texture.impl.filter.NormalFilter
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CameraWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/8/9.
 */
class VideoRecorderImpl(ctx: Context,
                        private var encoder: Encoder? = null,
                        private var audioEncoder: Encoder? = null,
                        private var cameraWrapper: CameraWrapper? = null,
                        private var render: Render? = null,
                        private var muxer: Muxer? = null,
                        private var onStateListener: VideoRecorder.OnStateListener? = null,
                        private var textureView: TextureView? = null,
                        private var status: Status = Status.IDL,
                        private var filter: Class<*>? = NormalFilter::class.java) : VideoRecorder {
    enum class Status {
        IDL, PREPARED, STARTED
    }

    private var context: CodecContext = CodecContext(ctx)

    override fun prepare() {
        if (TextUtils.isEmpty(context.ioContext.path)) {
            throw RuntimeException("context.ioContext.path can not be null!")
        }
        if (null == cameraWrapper) {
            cameraWrapper = CameraWrapper.open(context, this)
                    .post(Runnable {
                        render = DefaultRenderImpl(context, cameraWrapper!!.textureWrapper, filter)
                    })
        }
        if (null != textureView && textureView!!.isAvailable) {
            render?.post(Runnable {
                render?.updateSize(context.video.width, context.video.height)
                startEncoder()
            })
            return
        }
        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {
                updatePreview(p1, p2)
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
                stop()
                return true
            }

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                if (null != p0) {
                    startPreview(p0, p1, p2)
                }
                debug_e("onSurfaceTextureAvailable")
            }
        }
    }

    override fun prepared(): Boolean {
        return Status.PREPARED == status
    }

    override fun started(): Boolean {
        return Status.STARTED == status
    }

    override fun start() {
        if (Status.PREPARED != status) {
            throw IllegalStateException("The recorder is not prepare yet.")
        }
        encoder?.start()
        audioEncoder?.start()
        status = Status.STARTED
    }

    override fun pause() {
        if (Status.STARTED != status) {
            throw IllegalStateException("The recorder is not start yet.")
        }
        encoder?.pause()
        audioEncoder?.pause()
        status = Status.PREPARED
    }

    override fun stop() {
        if (Status.IDL == status) return
        GLEventPipeline.INSTANCE.queueEvent(Runnable {
            stopEncoder()
        })
        status = Status.IDL
    }

    override fun reset() {
        if (Status.IDL != status) {
            throw IllegalStateException("The recorder is prepared. You cannot reset a prepared recorder.")
        }
        context.reset()
    }

    private fun changeParamsCheck() {
        if (Status.IDL != status) {
            throw IllegalStateException("You cannot change a prepared recorder.")
        }
    }

    override fun setCameraIndex(index: CameraWrapper.CameraIndex) {
        if (Status.IDL == status) {
            context.cameraIndex = index
        } else if (Status.PREPARED == status || Status.STARTED == status) {
            cameraWrapper?.switchCamera(index)
        }
    }


    override fun enableHardware(enable: Boolean) {
        changeParamsCheck()
        context.codecType = if (enable) CodecContext.CodecType.HARD else CodecContext.CodecType.SOFT
    }

    override fun setOutputSize(width: Int, height: Int) {
        changeParamsCheck()
        context.video.width = width
        context.video.height = height
        if (0 != context.cameraSize.width && 0 != context.cameraSize.height) {
            context.check()
        }
    }

    override fun setVideoBitrate(bitrate: Int) {
        changeParamsCheck()
        context.video.bitrate = bitrate
    }

    /**
     * Set fps. It is highly recommended to set this value
     * @params fps 0 means auto
     */
    override fun setFps(fps: Int) {
        changeParamsCheck()
        context.video.fps = fps
    }

    override fun getWidth(): Int {
        return context.video.width
    }

    override fun getHeight(): Int {
        return context.video.height
    }

    override fun setFilter(filter: Class<*>) {
        if (null == render) {
            this.filter = filter
            if (null == this.filter)
                this.filter = NormalFilter::class.java
        } else {
            render?.setFilter(filter)
        }
    }

    override fun getFilter(): BaseFilter? {
        return render?.getFilter()
    }

    override fun setOutputUri(uri: String) {
        changeParamsCheck()
        context.ioContext.path = uri
    }

    override fun setPreviewDisplay(view: TextureView) {
        this.textureView = view
    }

    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
        render?.onFrameAvailable()
        render?.post(Runnable {
            encoder?.onFrameAvailable(cameraTexture)
        })
    }

    private fun startPreview(screenTexture: SurfaceTexture, width: Int, height: Int) {
        cameraWrapper?.post(Runnable {
            render?.start(screenTexture, width, height)
            render?.post(Runnable {
                render?.updateSize(context.video.width, context.video.height)
                if (TextUtils.isEmpty(context.ioContext.path)) {
                    throw RuntimeException("context.ioContext.path can not be null!")
                }
                startEncoder()
            })
        })
    }

    private fun updatePreview(width: Int, height: Int) {
//        mRender?.updatePreview(width, height)
    }

    private fun startEncoder() {
        if (null == muxer) {
            muxer = MuxerFactory.getMuxer(context)
            muxer?.onMuxerListener = object : Muxer.OnMuxerListener {
                override fun onError(error: Int, msg: String) {
                    onStateListener?.onError(error, msg)
                }
            }
        } else {
            muxer?.reset()
        }
        if (context.video.bitrate <= 0)
            setVideoBitrate(context.video.width * context.video.height * CodecContext.Video.MEDIUM * context.video.fps / 24)
        context.check()
        encoder = CodecFactory.getEncoder(context, render!!.getFrameBufferTexture(),
                cameraWrapper!!.textureWrapper.egl!!.eglContext!!).apply {
            onPreparedListener = object : Encoder.OnPreparedListener {
                override fun onPrepared(encoder: Encoder) {
                    status = Status.PREPARED
                    onStateListener?.onPrepared(encoder)
                }
            }
            if (null != muxer)
                setOnSampleListener(muxer!!)
        }
        audioEncoder = AudioEncoderImpl(context).apply {
            if (null != muxer)
                setOnSampleListener(muxer!!)
        }
        if (null != onStateListener)
            setOnStateListener(onStateListener!!)
    }

    override fun setOnStateListener(listener: VideoRecorder.OnStateListener) {
        this.onStateListener = listener
        encoder?.onRecordListener = onStateListener
    }

    override fun release() {
        stop()
        try {
            cameraWrapper?.release()
            cameraWrapper = null
            render?.release()
            render = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        context.release()
        GLEventPipeline.INSTANCE.quit()
    }

    private fun stopEncoder() {
        if (null != encoder) {
            encoder?.stop()
            encoder = null
        }
        if (null != audioEncoder) {
            audioEncoder?.stop()
            audioEncoder = null
        }
        if (null != muxer) {
            muxer?.release()
            muxer = null
        }
        onStateListener?.onStop()
    }
}