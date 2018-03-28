package com.lmy.codec.impl

import android.graphics.SurfaceTexture
import com.lmy.codec.Encoder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.render.impl.DefaultRender
import com.lmy.codec.wrapper.CameraWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraPreviewPresenter : SurfaceTexture.OnFrameAvailableListener {

    private val syncOp = Any()
    private var mCameraWrapper: CameraWrapper? = null
    private var mRender: DefaultRender? = null
    private var mEncoder: Encoder? = null
    private var isPreviewing: Boolean = false
    fun prepare(param: Parameter) {
        mCameraWrapper = CameraWrapper.open(param, this)
        mRender = DefaultRender(mCameraWrapper!!.textureWrapper)
        mEncoder = DefaultEncoder(param)
    }

    /**
     * Camera有数据生成时回调
     * For CameraWrapper
     */
    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
        mRender?.onFrameAvailable(cameraTexture)
    }

    fun startPreview(screenTexture: SurfaceTexture, width: Int, height: Int) {
        synchronized(syncOp) {
            if (!isPreviewing) {
                if (!mCameraWrapper!!.startPreview()) {
                    return
                }
            }
            mRender?.start(screenTexture, width, height)
            isPreviewing = true
        }
    }

    fun updatePreview(width: Int, height: Int) {
//        mRender?.updatePreview(width, height)
    }

    fun stopPreview() {
        synchronized(syncOp) {
            if (isPreviewing) {
                mRender?.stop()
                mCameraWrapper?.release()
            }
            isPreviewing = false
        }
    }

    interface OnVideoDataCallback {
        fun onFrame()
        fun onAudio()
    }

    fun release() {
        synchronized(syncOp) {
            try {
                mCameraWrapper?.release()
                mCameraWrapper = null
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                mRender?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}