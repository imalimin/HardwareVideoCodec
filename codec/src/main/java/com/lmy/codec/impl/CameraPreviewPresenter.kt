package com.lmy.codec.impl

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import com.lmy.codec.Encoder
import com.lmy.codec.Muxer
import com.lmy.codec.entity.Parameter
import com.lmy.codec.entity.Sample
import com.lmy.codec.render.impl.DefaultRender
import com.lmy.codec.util.debug_v
import com.lmy.codec.wrapper.CameraWrapper
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraPreviewPresenter(var encoder: Encoder? = null,
                             private var cameraWrapper: CameraWrapper? = null,
                             private var render: DefaultRender? = null,
                             private var muxer: Muxer? = null) : SurfaceTexture.OnFrameAvailableListener,
        Encoder.OnSampleListener {

    private val syncOp = Any()
    private var isPreviewing: Boolean = false
    fun prepare(param: Parameter) {
        cameraWrapper = CameraWrapper.open(param, this)
        render = DefaultRender(cameraWrapper!!.textureWrapper)
        encoder = DefaultEncoder(param, cameraWrapper!!.textureWrapper)
        encoder!!.setOnSampleListener(this)
        muxer = MuxerImpl(param, "/storage/emulated/0/test.mp4")
    }

    /**
     * 编码后的帧数据
     * For DefaultEncoder
     */
    override fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer) {
        debug_v("onSample: ${info.size}")
        muxer?.write(Sample.wrap(info, data))
    }

    /**
     * Camera有数据生成时回调
     * For CameraWrapper
     */
    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
        render?.onFrameAvailable(cameraTexture)
        encoder?.onFrameAvailable(cameraTexture)
    }

    fun startPreview(screenTexture: SurfaceTexture, width: Int, height: Int) {
        synchronized(syncOp) {
            if (!isPreviewing) {
                if (!cameraWrapper!!.startPreview()) {
                    return
                }
            }
            render?.start(screenTexture, width, height)
            isPreviewing = true
        }
    }

    fun updatePreview(width: Int, height: Int) {
//        mRender?.updatePreview(width, height)
    }

    fun stopPreview() {
        synchronized(syncOp) {
            if (isPreviewing) {
                release()
            }
            isPreviewing = false
        }
    }

    interface OnVideoDataCallback {
        fun onFrame()
        fun onAudio()
    }

    private fun release() {
        synchronized(syncOp) {
            try {
                render?.stop()
                render?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                cameraWrapper?.release()
                cameraWrapper = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
            encoder?.stop(object : Encoder.OnStopListener {
                override fun onStop() {
                    muxer?.release()
                }
            })

        }
    }
}