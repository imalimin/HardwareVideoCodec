package com.lmy.codec.entity

import android.hardware.Camera

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class Parameter(var width: Int = 720,
                var height: Int = 1280,
                var fps: Int = 24,
                var cameraIndex: Int = Camera.CameraInfo.CAMERA_FACING_BACK,
                var previewWidth: Int = height,
                var previewHeight: Int = width) {
    fun check() {
        if (width > previewWidth || height > previewHeight)
            throw RuntimeException("Encode size can not be greater than preview size")
    }
}