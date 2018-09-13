/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import android.hardware.Camera
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.helper.CameraHelper
import com.lmy.codec.pipeline.impl.GLEventPipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_v

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraWrapper(private var context: CodecContext,
                    private var onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener) {
    enum class CameraIndex { BACK, FRONT }
    companion object {
        private val PREPARE = 0x1
        fun open(param: CodecContext, onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener)
                : CameraWrapper {
            return CameraWrapper(param, onFrameAvailableListener)
        }
    }

    private var mCamera: Camera? = null
    private var mCameras = 0
    private var mCameraIndex: CameraIndex? = null
    val textureWrapper: CameraTextureWrapper

    init {
        mCameras = CameraHelper.getNumberOfCameras()
        textureWrapper = CameraTextureWrapper(context.video.width, context.video.height)
        openCamera(context.cameraIndex)
    }

    fun post(runnable: Runnable): CameraWrapper {
        GLEventPipeline.INSTANCE.queueEvent(runnable)
        return this
    }

    fun switchCamera(index: CameraIndex) {
        openCamera(index)
    }

    private fun openCamera(index: CameraIndex) {
        val tmp = if (index == CameraIndex.FRONT && mCameras < 2) {//如果没有前置摄像头，则强制使用后置摄像头
            CameraIndex.BACK
        } else {
            index
        }
        if (null != mCameraIndex && mCameraIndex == tmp) //如果已经打开过摄像头，并且当前已经是index，则不做改变
            return
        mCameraIndex = tmp
        context.cameraIndex = tmp
        GLEventPipeline.INSTANCE.queueEvent(Runnable {
            stopPreview()
            updateTexture()
            prepare()
            textureWrapper.updateLocation(context)
            startPreview()
        })
    }

    private fun updateTexture() {
        textureWrapper.updateTexture()
        textureWrapper.surfaceTexture!!.setOnFrameAvailableListener(onFrameAvailableListener)
    }

    private fun getCameraIndex(): Int {
        if (context.cameraIndex == CameraIndex.FRONT)
            return Camera.CameraInfo.CAMERA_FACING_FRONT
        return Camera.CameraInfo.CAMERA_FACING_BACK
    }

    private fun prepare() {
        if (0 == mCameras) {
            debug_e("Unavailable camera")
            return
        }

        val time = System.currentTimeMillis()
        mCamera = openCamera(getCameraIndex())
        debug_e("open time: ${System.currentTimeMillis() - time}")
        if (null == mCamera) {
            debug_e("mCamera is null!")
            return
        }
        val cameraParam = mCamera!!.parameters
        CameraHelper.setPreviewSize(cameraParam, context)
        CameraHelper.setColorFormat(cameraParam, context)
        CameraHelper.setFocusMode(cameraParam, context)
        CameraHelper.setFps(cameraParam, context)
        CameraHelper.setAutoExposureLock(cameraParam, false)
        CameraHelper.setSceneMode(cameraParam, Camera.Parameters.SCENE_MODE_AUTO)
        CameraHelper.setFlashMode(cameraParam, Camera.Parameters.FLASH_MODE_OFF)
        CameraHelper.setAntibanding(cameraParam, Camera.Parameters.ANTIBANDING_AUTO)
        CameraHelper.setVideoStabilization(cameraParam, true)
        val fps = IntArray(2)
        cameraParam.getPreviewFpsRange(fps)
        debug_v("Config: Size(${context.cameraSize.width}x${context.cameraSize.height})\n" +
                "Format(${cameraParam.previewFormat})\n" +
                "FocusMode(${cameraParam.focusMode})\n" +
                "Fps(${fps[0]}-${fps[1]})\n" +
                "AutoExposureLock(${cameraParam.autoExposureLock})\n" +
                "SceneMode(${cameraParam.sceneMode})\n" +
                "FlashMode(${cameraParam.flashMode})\n" +
                "Antibanding(${cameraParam.antibanding})\n" +
                "VideoStabilization(${cameraParam.videoStabilization})")
        try {
            mCamera!!.parameters = cameraParam
            debug_e("Camera config")
        } catch (e: Exception) {
            mCamera?.release()
            debug_e("Camera $mCameraIndex open failed. Please check parameters")
        }
    }

    private fun openCamera(index: Int): Camera? {
        return try {
            val camera = Camera.open(index)
            camera.setDisplayOrientation(context.orientation)
            camera
        } catch (e: SecurityException) {
            debug_e("Camera $index open failed, No permission")
            e.printStackTrace()
            null
        } catch (e: Exception) {
            debug_e("Camera $index open failed")
            e.printStackTrace()
            null
        }
    }

    fun release() {
        GLEventPipeline.INSTANCE.queueEvent(Runnable {
            stopPreview()
            releaseTexture()
        })
    }

    private fun startPreview() {
        if (null == mCamera) {
            debug_e("Start preview failed, mCamera is null")
            return
        }
        try {
            mCamera!!.setPreviewTexture(textureWrapper.surfaceTexture)
            mCamera!!.startPreview()
        } catch (e: Exception) {
            release()
            debug_e("Start preview failed")
            e.printStackTrace()
        }
    }

    private fun stopPreview() {
        if (null == mCamera) return
        try {
            mCamera!!.setPreviewTexture(null)
            mCamera!!.setPreviewCallback(null)
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
        } catch (e: Exception) {
            debug_e("Stop preview failed")
            e.printStackTrace()
        }
    }

    private fun releaseTexture() {
        textureWrapper.release()
    }
}