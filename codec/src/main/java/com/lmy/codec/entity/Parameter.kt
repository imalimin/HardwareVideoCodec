/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.entity

import android.content.Context
import android.hardware.Camera
import android.media.AudioFormat
import android.media.MediaCodecInfo

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
data class Parameter(var context: Context,
                     var video: Video = Video(),
                     var audio: Audio = Audio(),
                     var cameraIndex: Int = Camera.CameraInfo.CAMERA_FACING_BACK,
                     var previewWidth: Int = 1280,//以水平分辨率为准
                     var previewHeight: Int = 720,//以水平分辨率为准
                     var orientation: Int = 90,
                     var codecType: CodecType = CodecType.HARD) {
    fun check() {
        if (!isHorizontal() && !isVertical())
            throw RuntimeException("Orientation must be 0, 90, 180 or 270")
        if (isVertical() && (video.width > previewHeight || video.height > previewWidth)
                || isHorizontal() && (video.width > previewWidth || video.height > previewHeight))
            throw RuntimeException("Video size can not be greater than preview size")
    }

    fun isHorizontal(): Boolean {
        return 0 == orientation || 180 == orientation
    }

    fun isVertical(): Boolean {
        return 90 == orientation || 270 == orientation
    }

    data class Video(var mime: String = "video/avc",
                     var width: Int = 720,
                     var height: Int = 1080,
                     var fps: Int = 30,//If not support, select the lowest fps
                     var bitrate: Int = width * height * MEDIUM * fps / 24,
                     var iFrameInterval: Int = 2,
                     var bitrateMode: Int = 1,//if support, default VBR
                     /**
                      * if support, default High. It will be change by CodecHelper#createVideoFormat
                      * {@link MediaCodecInfo.CodecProfileLevel}
                      */
                     var profile: Int = 0x08,
                     /**
                      * if support, default level 31. It will be change by CodecHelper#createVideoFormat
                      */
                     var level: Int = 0x200
    ) {
        companion object {
            const val HIGH = 5
            const val MEDIUM = 3
            const val LOW = 1
        }
    }

    data class Audio(var mime: String = "audio/mp4a-latm",
                     var channel: Int = 1,
                     var sampleRateInHz: Int = 16000,
                     var bitrate: Int = 96000,
                     var profile: Int = MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                     var pcm: Int = AudioFormat.ENCODING_PCM_16BIT)

    enum class CodecType {
        HARD, SOFT
    }
}