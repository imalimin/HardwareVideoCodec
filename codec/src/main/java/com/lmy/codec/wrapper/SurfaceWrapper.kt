package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class SurfaceWrapper {
    private val syncOp = Any()
    private var mDrawHandlerThread = HandlerThread("HandlerThread-SurfaceWrapper")
    private var mGLHandler: GLHandler? = null

    init {
        mDrawHandlerThread.start()
        mGLHandler = GLHandler(mDrawHandlerThread.looper)
        mGLHandler!!.sendEmptyMessage(GLHandler.WHAT_INIT)
    }

    fun updateCameraTexture(texture: SurfaceTexture) {
        synchronized(syncOp) {
            if (null != mGLHandler) {
                mGLHandler!!.updateCameraTexture(texture)
            }
        }
    }

    fun startPreview(surface: SurfaceTexture, width: Int, height: Int) {

    }

    private class GLHandler(private val myLooper: Looper) : Handler() {
        private var cameraTexture: SurfaceTexture? = null
        private val screenTexture: SurfaceTexture? = null
        private val syncCameraTextureOp = Any()
        private val syncScreenTextureOp = Any()

        fun updateCameraTexture(texture: SurfaceTexture) {
            synchronized(syncCameraTextureOp) {
                if (texture !== cameraTexture) {
                    cameraTexture = texture
                }
            }
        }
    }
}