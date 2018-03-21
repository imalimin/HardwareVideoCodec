package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import android.hardware.Camera
import com.lmy.codec.Config
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CameraHelper
import com.lmy.codec.loge
import com.lmy.codec.logv

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraWrapper(private var parameter: Parameter,
                    private var onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener,
                    var surfaceTexture: SurfaceTexture? = null) {
    companion object {
        val TEXTURE_ID = 10
        fun open(param: Parameter, onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener)
                : CameraWrapper {
            return CameraWrapper(param, onFrameAvailableListener)
        }
    }

    private var mCamera: Camera? = null
    private var mCameras = 0
    private var mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK

    init {
        mCameras = CameraHelper.getNumberOfCameras()
        prepare()
    }

    private fun prepare() {
        if (0 == mCameras) {
            loge(this, "Unavailable camera")
            return
        }
        //如果没有前置摄像头，则强制使用后置摄像头
        if (parameter.cameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT && mCameras < 2)
            parameter.cameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK
        mCameraIndex = parameter.cameraIndex

        mCamera = openCamera(mCameraIndex)
        if (null == mCamera) return
        val cameraParam = mCamera!!.parameters
        CameraHelper.setPreviewSize(cameraParam, parameter)
        CameraHelper.setColorFormat(cameraParam, parameter)
        CameraHelper.setFocusMode(cameraParam, parameter)
        CameraHelper.setFps(cameraParam, parameter)
        CameraHelper.setAutoExposureLock(cameraParam, false)
        CameraHelper.setSceneMode(cameraParam, Camera.Parameters.SCENE_MODE_AUTO)
        CameraHelper.setFlashMode(cameraParam, Camera.Parameters.FLASH_MODE_OFF)
        CameraHelper.setAntibanding(cameraParam, Camera.Parameters.ANTIBANDING_AUTO)
        CameraHelper.setVideoStabilization(cameraParam, true)
        if (Config.Debug) {
            val fps = IntArray(2)
            cameraParam.getPreviewFpsRange(fps)
            logv(this, "Config: Size(${parameter.previewWidth}x${parameter.previewHeight})\n" +
                    "Format(${cameraParam.previewFormat})\n" +
                    "FocusMode(${cameraParam.focusMode})\n" +
                    "Fps(${fps[0]}-${fps[1]})\n" +
                    "AutoExposureLock(${cameraParam.autoExposureLock})\n" +
                    "SceneMode(${cameraParam.sceneMode})\n" +
                    "FlashMode(${cameraParam.flashMode})\n" +
                    "Antibanding(${cameraParam.antibanding})\n" +
                    "VideoStabilization(${cameraParam.videoStabilization})")
        }
        try {
            mCamera!!.parameters = cameraParam
        } catch (e: Exception) {
            mCamera!!.release()
            loge(this, "Camera $mCameraIndex open failed. Please check parameters")
        }
    }

    private fun openCamera(index: Int): Camera? {
        return try {
            val camera = Camera.open(index)
            camera.setDisplayOrientation(0)
            camera
        } catch (e: SecurityException) {
            loge(this, "Camera $index open failed, No permission")
            e.printStackTrace()
            null
        } catch (e: Exception) {
            loge(this, "Camera $index open failed")
            e.printStackTrace()
            null
        }
    }

    private fun release() {
        if (null == mCamera) return
        mCamera!!.release()
        mCamera = null
    }

    fun startPreview(): Boolean {
        surfaceTexture = SurfaceTexture(TEXTURE_ID)
        if (null == mCamera) return false
        surfaceTexture!!.setOnFrameAvailableListener(onFrameAvailableListener)
        try {
            mCamera!!.setPreviewTexture(surfaceTexture)
            mCamera!!.startPreview()
            return true
        } catch (e: Exception) {
            release()
            loge(this, "Start preview failed")
            e.printStackTrace()
            return false
        }
    }

    fun stopPreview() {
        if (null == mCamera) return
        try {
            mCamera!!.stopPreview()
        } catch (e: Exception) {
            loge(this, "Stop preview failed")
            e.printStackTrace()
        }
    }

    fun releaseTexture() {
        if (null == surfaceTexture) return
        surfaceTexture!!.release()
        surfaceTexture = null
    }
}