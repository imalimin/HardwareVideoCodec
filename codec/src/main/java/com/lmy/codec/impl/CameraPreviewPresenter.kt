package com.lmy.codec.impl

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import com.lmy.codec.Encoder
import com.lmy.codec.Muxer
import com.lmy.codec.entity.Parameter
import com.lmy.codec.entity.Sample
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRenderImpl
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.CameraWrapper
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraPreviewPresenter(var parameter: Parameter,
                             var encoder: Encoder? = null,
                             var audioEncoder: Encoder? = null,
                             private var cameraWrapper: CameraWrapper? = null,
                             private var render: Render? = null,
                             private var muxer: Muxer? = MuxerImpl("/storage/emulated/0/test.mp4"))
    : SurfaceTexture.OnFrameAvailableListener, Encoder.OnSampleListener {

    private val syncOp = Any()
    private var onStateListener: OnStateListener? = null
    private val onAudioSampleListener: Encoder.OnSampleListener = object : Encoder.OnSampleListener {
        override fun onFormatChanged(format: MediaFormat) {
            debug_e("Add audio track")
            muxer?.addAudioTrack(format)
        }

        override fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer) {
            debug_e("audio sample(${info.size})")
            muxer?.writeAudioSample(Sample.wrap(info, data))
        }
    }

    init {
        cameraWrapper = CameraWrapper.open(parameter, this)
        render = DefaultRenderImpl(parameter, cameraWrapper!!.textureWrapper as CameraTextureWrapper)
    }

    override fun onFormatChanged(format: MediaFormat) {
        muxer?.addVideoTrack(format)
    }

    /**
     * 编码后的帧数据
     * For VideoEncoderImpl
     */
    override fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer) {
        muxer?.writeVideoSample(Sample.wrap(info, data))
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
            render?.start(screenTexture, width, height, Runnable {
                encoder = SoftVideoEncoderImpl(parameter,
                        cameraWrapper!!.textureWrapper as CameraTextureWrapper)
                encoder!!.setOnSampleListener(this@CameraPreviewPresenter)
                audioEncoder = AudioEncoderImpl(parameter)
                audioEncoder!!.setOnSampleListener(onAudioSampleListener)
            })
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
        }
        stopEncoder()
    }

    private fun stopEncoder() {
        encoder?.stop(object : Encoder.OnStopListener {
            override fun onStop() {
                audioEncoder?.stop(object : Encoder.OnStopListener {
                    override fun onStop() {
                        muxer?.release()
                        onStateListener?.onStop()
                    }
                })
            }
        })
    }

    fun setOnStateListener(listener: OnStateListener) {
        onStateListener = listener
    }

    interface OnStateListener {
        fun onStop()
    }
}