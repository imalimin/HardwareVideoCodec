package com.lmy.codec.impl

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import com.lmy.codec.Encoder
import com.lmy.codec.Muxer
import com.lmy.codec.entity.Parameter
import com.lmy.codec.entity.Sample
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRender
import com.lmy.codec.util.debug_v
import com.lmy.codec.wrapper.CameraWrapper
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraPreviewPresenter(var parameter: Parameter,
                             var encoder: Encoder? = null,
                             private var cameraWrapper: CameraWrapper? = null,
                             private var render: Render? = null,
                             private var muxer: Muxer? = null) : SurfaceTexture.OnFrameAvailableListener,
        Encoder.OnSampleListener {

    private val syncOp = Any()

    init {
        cameraWrapper = CameraWrapper.open(parameter, this)
        render = DefaultRender(cameraWrapper!!.textureWrapper)
        encoder = DefaultEncoder(parameter, cameraWrapper!!.textureWrapper)
        encoder!!.setOnSampleListener(this)
    }

    override fun onFormatChanged(format: MediaFormat) {
        muxer = MuxerImpl(format, "/storage/emulated/0/test.mp4")
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
            cameraWrapper!!.startPreview()
            render?.start(screenTexture, width, height)
        }
    }

    fun updatePreview(width: Int, height: Int) {
//        mRender?.updatePreview(width, height)
    }

    fun stopPreview() {
        synchronized(syncOp) {
            release()
        }
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