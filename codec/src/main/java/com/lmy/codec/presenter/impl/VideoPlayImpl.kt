package com.lmy.codec.presenter.impl

import android.graphics.SurfaceTexture
import android.view.TextureView
import com.lmy.codec.decoder.Decoder
import com.lmy.codec.decoder.impl.HardVideoDecoderImpl
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.presenter.VideoPlay
import com.lmy.codec.texture.impl.filter.BaseFilter
import java.io.File

class VideoPlayImpl : VideoPlay {
    private var pipeline: Pipeline? = EventPipeline.create("VideoPlayImpl")
    private var decoder: Decoder? = null
    private var view: TextureView? = null
    private var file: File? = null

    private fun check() {
        if (null == file) {
            throw RuntimeException("Please call setInputResource before call prepare.")
        }
        if (null == view) {
            throw RuntimeException("Please call setPreviewDisplay before call prepare.")
        }
    }

    private fun initDecoder(texture: SurfaceTexture, width: Int, height: Int) {
        decoder = HardVideoDecoderImpl(texture)
        decoder?.setInputResource(file!!.absolutePath)
        decoder?.prepare()
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
                    stop()
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
        pipeline?.queueEvent(Runnable {
            decoder?.release()
            decoder = null
        })
        pipeline?.quit()
        pipeline = null
        view = null
    }

    override fun setFilter(filter: BaseFilter) {

    }

    override fun getFilter(): BaseFilter? {
        return null
    }
}