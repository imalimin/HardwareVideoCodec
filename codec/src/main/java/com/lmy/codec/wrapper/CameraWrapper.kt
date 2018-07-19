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
import com.lmy.codec.pipeline.SingleEventPipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_v

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraWrapper(private var context: CodecContext,
                    private var onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener) {
    companion object {
        private val PREPARE = 0x1
        fun open(param: CodecContext, onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener)
                : CameraWrapper {
            return CameraWrapper(param, onFrameAvailableListener)
        }
    }

    private var mCamera: Camera? = null
    private var mCameras = 0
    private var mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK
    lateinit var textureWrapper: CameraTextureWrapper

    init {
        mCameras = CameraHelper.getNumberOfCameras()
        SingleEventPipeline.instance.queueEvent(Runnable {
            textureWrapper = CameraTextureWrapper(context.video.width, context.video.height)
            textureWrapper.updateSize(context.previewWidth, context.previewHeight,
                    context.video.width, context.video.height)
            textureWrapper.surfaceTexture!!.setOnFrameAvailableListener(onFrameAvailableListener)
        })
        SingleEventPipeline.instance.queueEvent(Runnable { prepare() })
    }

    fun post(runnable: Runnable): CameraWrapper {
        SingleEventPipeline.instance.queueEvent(runnable)
        return this
    }

    private fun prepare() {
        if (0 == mCameras) {
            debug_e("Unavailable camera")
            return
        }
        //如果没有前置摄像头，则强制使用后置摄像头
        if (context.cameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT && mCameras < 2)
            context.cameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK
        mCameraIndex = context.cameraIndex

        val time = System.currentTimeMillis()
        mCamera = openCamera(mCameraIndex)
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
        debug_v("Config: Size(${context.previewWidth}x${context.previewHeight})\n" +
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
            context.check()
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
        SingleEventPipeline.instance.queueEvent(Runnable {
            stopPreview()
            releaseTexture()
        })
    }

    fun startPreview() {
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