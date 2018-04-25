/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.helper

import android.graphics.ImageFormat
import android.hardware.Camera
import com.lmy.codec.entity.Parameter
import com.lmy.codec.util.debug_v

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraHelper {

    companion object {
        fun getNumberOfCameras(): Int {
            return Camera.getNumberOfCameras()
        }

        fun setPreviewSize(cameraParam: Camera.Parameters, videoParam: Parameter) {
            val supportSizes = cameraParam.supportedPreviewSizes
            var bestWidth = 0
            var bestHeight = 0
            for (size in supportSizes) {
                if (size.width >= videoParam.previewWidth//预览宽大于输出宽
                        && size.height >= videoParam.previewHeight//预览高大于输出高
                        && (size.width * size.height < bestWidth * bestHeight || 0 == bestWidth * bestHeight)) {//选择像素最少的分辨率
                    bestWidth = size.width
                    bestHeight = size.height
                }
            }
            debug_v("target preview size: " + videoParam.previewWidth + "x" + videoParam.previewHeight + ", best: " + bestWidth + "x" + bestHeight)
            videoParam.previewWidth = bestWidth
            videoParam.previewHeight = bestHeight
            videoParam.check()
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
                if (it[0] == it[1] && it[0] == videoParam.video.fps * 1000) {
                    fps = it
                    return@forEach
                }
                if (videoParam.video.fps * 1000 >= it[0] && videoParam.video.fps * 1000 <= it[1]
                        && (it[0] > fps[0] || it[1] < fps[1]))
                    fps = it
            }
            cameraParam.setPreviewFpsRange(fps[0], fps[1])
            debug_v("fps: ${fps[0]}-${fps[1]}, target: ${videoParam.video.fps * 1000}")
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