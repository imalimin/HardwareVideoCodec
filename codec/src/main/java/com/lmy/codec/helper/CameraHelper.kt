package com.lmy.codec.helper

import android.graphics.ImageFormat
import android.hardware.Camera
import com.lmy.codec.Config
import com.lmy.codec.entity.Parameter
import com.lmy.codec.logv

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraHelper {

    companion object {
        fun getNumberOfCameras(): Int {
            return Camera.getNumberOfCameras()
        }

        fun setPreviewSize(cameraParam: Camera.Parameters, videoParam: Parameter) {
            cameraParam.setPreviewSize(videoParam.previewWidth, videoParam.previewHeight)
        }

        fun setColorFormat(cameraParam: Camera.Parameters, videoParam: Parameter) {
            if (cameraParam.supportedPreviewFormats.contains(ImageFormat.NV21))
                cameraParam.previewFormat = ImageFormat.NV21
        }

        fun setFocusMode(cameraParam: Camera.Parameters, videoParam: Parameter) {
            val modes = cameraParam.supportedFocusModes ?: return
            when {
                modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) -> cameraParam.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                modes.contains(Camera.Parameters.FOCUS_MODE_AUTO) -> cameraParam.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                modes.contains(Camera.Parameters.FOCUS_MODE_FIXED) -> cameraParam.focusMode = Camera.Parameters.FOCUS_MODE_FIXED
            }
        }

        fun setFps(cameraParam: Camera.Parameters, videoParam: Parameter) {
            val fpsRanges = cameraParam.supportedPreviewFpsRange
            var fps = fpsRanges[0]
            fpsRanges.forEach {
                if (it[0] == it[1] && it[0] == videoParam.fps * 1000) {
                    fps = it
                    return@forEach
                }
                if (videoParam.fps * 1000 >= it[0] && videoParam.fps * 1000 <= it[1]
                        && (it[0] > fps[0] || it[1] < fps[1]))
                    fps = it
            }
            cameraParam.setPreviewFpsRange(fps[0], fps[1])
            if (Config.Debug)
                logv(this, "fps: ${fps[0]}-${fps[1]}, target: ${videoParam.fps * 1000}")
        }

        fun setAutoExposureLock(cameraParam: Camera.Parameters, flag: Boolean) {
            if (cameraParam.isAutoExposureLockSupported)
                cameraParam.autoExposureLock = flag
        }

        fun setSceneMode(cameraParam: Camera.Parameters, mode: String) {
            if (cameraParam.supportedSceneModes != null && cameraParam.supportedSceneModes.contains(mode)) {
                cameraParam.sceneMode = mode
            }
        }

        fun setFlashMode(cameraParam: Camera.Parameters, mode: String) {
            if (cameraParam.supportedFlashModes != null && cameraParam.supportedFlashModes.contains(mode)) {
                cameraParam.flashMode = mode
            }
        }

        fun setAntibanding(cameraParam: Camera.Parameters, mode: String) {
            if (cameraParam.supportedAntibanding != null && cameraParam.supportedAntibanding.contains(mode)) {
                cameraParam.antibanding = mode
            }
        }

        fun setVideoStabilization(cameraParam: Camera.Parameters, flag: Boolean) {
            if (cameraParam.isVideoStabilizationSupported) {
                cameraParam.videoStabilization = flag
            }
        }
    }
}