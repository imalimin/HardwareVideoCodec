/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lmy.codec

import android.graphics.SurfaceTexture
import android.os.Environment
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.encoder.impl.AudioEncoderImpl
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CodecFactory
import com.lmy.codec.muxer.Muxer
import com.lmy.codec.muxer.impl.MuxerImpl
import com.lmy.codec.pipeline.SingleEventPipeline
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRenderImpl
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.wrapper.CameraWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraPreviewPresenter(var parameter: Parameter,
                             var encoder: Encoder? = null,
                             var audioEncoder: Encoder? = null,
                             private var cameraWrapper: CameraWrapper? = null,
                             private var render: Render? = null,
                             private var muxer: Muxer? = MuxerImpl("${Environment.getExternalStorageDirectory().absolutePath}/test.mp4"))
    : SurfaceTexture.OnFrameAvailableListener {

    private var onStateListener: OnStateListener? = null

    init {
        SingleEventPipeline.instance.start()
        cameraWrapper = CameraWrapper.open(parameter, this)
                .post(Runnable {
                    render = DefaultRenderImpl(parameter, cameraWrapper!!.textureWrapper)
                })
    }

    fun setFilter(filter: Class<*>) {
        render?.setFilter(filter)
    }

    fun getFilter(): BaseFilter? {
        return render?.getFilter()
    }

    /**
     * Camera有数据生成时回调
     * For CameraWrapper
     */
    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
        render?.onFrameAvailable()
        render?.post(Runnable {
            encoder?.onFrameAvailable(cameraTexture)
        })
    }

    fun startPreview(screenTexture: SurfaceTexture, width: Int, height: Int) {
        cameraWrapper?.post(Runnable {
            cameraWrapper!!.startPreview()
            render?.start(screenTexture, width, height)
            render?.post(Runnable {
                encoder = CodecFactory.getEncoder(parameter, render!!.getFrameBufferTexture(),
                        cameraWrapper!!.textureWrapper.egl!!.eglContext!!)
                audioEncoder = AudioEncoderImpl(parameter)
                if (null != muxer) {
                    encoder!!.setOnSampleListener(muxer!!)
                    audioEncoder!!.setOnSampleListener(muxer!!)
                }
            })
        })
    }

    fun updatePreview(width: Int, height: Int) {
//        mRender?.updatePreview(width, height)
    }

    fun updateSize(width: Int, height: Int) {
        render?.updateSize(width, height)
    }

    fun stopPreview() {
        release()
        SingleEventPipeline.instance.quit()
    }

    private fun release() {
        stopEncoder()
        try {
            cameraWrapper?.release()
            cameraWrapper = null
            render?.release()
            render = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopEncoder() {
        SingleEventPipeline.instance.queueEvent(Runnable {
            encoder?.stop()
            audioEncoder?.stop()
            muxer?.release()
            onStateListener?.onStop()
        })
    }

    fun setOnStateListener(listener: OnStateListener) {
        onStateListener = listener
    }

    interface OnStateListener {
        fun onStop()
    }
}