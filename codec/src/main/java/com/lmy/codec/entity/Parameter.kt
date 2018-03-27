package com.lmy.codec.entity

import android.hardware.Camera

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class Parameter(var width: Int = 720,
                var height: Int = 1280,
                var fps: Int = 24,
                var cameraIndex: Int = Camera.CameraInfo.CAMERA_FACING_BACK,
                var previewWidth: Int = height,//以水平分辨率为准
                var previewHeight: Int = width,//以水平分辨率为准
                var orientation: Int = 10) {
    fun check() {
        if (!isHorizontal() && !isVertical())
            throw RuntimeException("Orientation must be 0, 90, 180 or 270")
        if (isVertical() && (width > previewHeight || height > previewWidth)
                || isHorizontal() && (width > previewWidth || height > previewHeight))
            throw RuntimeException("Video size can not be greater than preview size")
    }

    fun isHorizontal(): Boolean {
        return 0 == orientation || 180 == orientation
    }

    fun isVertical(): Boolean {
        return 90 == orientation || 270 == orientation
    }
}