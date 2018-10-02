package com.lmy.codec.presenter.impl

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.TextureView
import com.lmy.codec.decoder.Decoder
import com.lmy.codec.decoder.impl.HardVideoDecoderImpl
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.presenter.VideoPlay
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRenderImpl
import com.lmy.codec.texture.impl.filter.BaseFilter
import java.io.File

class VideoPlayImpl(ctx: Context) : VideoPlay, SurfaceTexture.OnFrameAvailableListener {
    private var context: CodecContext = CodecContext(ctx).apply {
        orientation = 0
    }
    private var render: Render? = null
    private var pipeline: Pipeline? = EventPipeline.create("VideoPlayImpl")
    private var decoder: Decoder? = HardVideoDecoderImpl(context, pipeline!!, true)
            .apply {
                onFrameAvailableListener = this@VideoPlayImpl
            }
    private var view: TextureView? = null
    private var file: File? = null
    private var filter: BaseFilter? = null

    private fun check() {
        if (null == file) {
            throw RuntimeException("Please call setInputResource before call prepare.")
        }
        if (null == view) {
            throw RuntimeException("Please call setPreviewDisplay before call prepare.")
        }
    }

    private fun initDecoder(texture: SurfaceTexture, width: Int, height: Int) {
        decoder?.setInputResource(file!!.absolutePath)
        decoder?.prepare()
        decoder?.post(Runnable {
            render = DefaultRenderImpl(context, decoder!!.textureWrapper!!, pipeline!!, filter)
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
                initDecoder(this.view!!.surfaceTexture, this.view!!.width, this.view!!.height)
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
                            initDecoder(texture, width, height)
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
        })
    }

    override fun pause() {
        pipeline?.queueEvent(Runnable {
            decoder?.pause()
        })
    }

    override fun stop() {
        pipeline?.queueEvent(Runnable {
            decoder?.stop()
        })
    }

    override fun setInputResource(file: File) {
        this.file = file
    }

    override fun setPreviewDisplay(view: TextureView) {
        this.view = view
    }

    override fun release() {
        render?.release()
        render = null
        pipeline?.queueEvent(Runnable {
            decoder?.release()
            decoder = null
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
}