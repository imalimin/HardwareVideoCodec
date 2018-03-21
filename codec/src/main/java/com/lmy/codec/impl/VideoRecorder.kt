package com.lmy.codec.impl

import android.graphics.SurfaceTexture
import com.lmy.codec.IRecorder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.wrapper.CameraWrapper
import com.lmy.codec.wrapper.SurfaceWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class VideoRecorder : IRecorder, SurfaceTexture.OnFrameAvailableListener {

    private val syncOp = Any()
    private var mCameraWrapper: CameraWrapper? = null
    private var mSurfaceWrapper: SurfaceWrapper? = null
    override fun prepare(param: Parameter) {
        mCameraWrapper = CameraWrapper.open(param, this)
        mSurfaceWrapper = SurfaceWrapper()
    }

    /**
     * For CameraWrapper
     */
    override fun onFrameAvailable(p0: SurfaceTexture?) {

    }

    override fun startPreview(surface: SurfaceTexture, width: Int, height: Int) {
        synchronized(syncOp) {
            if (!mCameraWrapper!!.startPreview()) {
                return
            }
            mSurfaceWrapper!!.updateCameraTexture(mCameraWrapper?.surfaceTexture!!)
            mSurfaceWrapper!!.startPreview(surface, width, height)
        }
    }

    override fun updatePreview(width: Int, height: Int) {
    }

    override fun stopPreview() {
    }

    interface OnVideoDataCallback {
        fun onFrame()
        fun onAudio()
    }
}