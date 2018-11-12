/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.entity

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodecInfo
import com.lmy.codec.helper.Resources
import com.lmy.codec.media.CameraWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CodecContext(ctx: Context,
                   internal var video: Video = Video(),
                   internal var audio: Audio = Audio(),
                   internal var ioContext: IOContext = IOContext(),
                   internal var cameraIndex: CameraWrapper.CameraIndex = CameraWrapper.CameraIndex.BACK,
                   internal val cameraSize: Size = Size(),//vaer size, Do not touch this value.
                   internal val viewSize: Size = Size(),//TextureView size, Do not touch this value.
                   internal var orientation: Int = 90,
                   internal var codecType: CodecType = CodecType.HARD) {
    init {
        Resources.instance.attach(ctx)
    }

    internal fun reset() {
        video = Video().apply {
            fps = video.fps//因为重置的时候，可能并不会重新打开摄像头，无法获取fps，所以不重置fps
        }
        audio = Audio()
        ioContext = IOContext()
        codecType = CodecType.HARD
    }

    fun release() {
        Resources.instance.dettach()
    }

    fun check() {
        if (!isHorizontal() && !isVertical())
            throw RuntimeException("Orientation must be 0, 90, 180 or 270")
        if ((isVertical() && (video.width > cameraSize.height || video.height > cameraSize.width))
                || (isHorizontal() && (video.width > cameraSize.width || video.height > cameraSize.height)))
            throw RuntimeException("Video size can not be greater than preview size " +
                    "Video(${video.width}x${video.height})," +
                    "Preview(${cameraSize.width}x${cameraSize.height})")
        if (0 != video.width % 2 || 0 != video.height % 2)
            throw RuntimeException("Video width and height must be a multiple of 2")
        if (video.fps < 1)
            throw RuntimeException("Video fps must be greater than 0")
        if (video.bitrate < 1)
            throw RuntimeException("Video bitrate must be greater than 0")
    }

    fun isHorizontal(): Boolean {
        return 0 == orientation || 180 == orientation
    }

    fun isVertical(): Boolean {
        return 90 == orientation || 270 == orientation
    }

    data class Video(internal var mime: String = "video/avc",
                     internal var width: Int = 720,
                     internal var height: Int = 1280,
                     internal var fps: Int = 0,//If not support, select the lower fps. 0 means auto.
                     internal var bitrate: Int = width * height * MEDIUM,
                     internal var iFrameInterval: Int = 2,
                     internal var bitrateMode: Int = BITRATE_MODE_CBR,//if support, default CBR
                     /**
                      * if support, default High. It will be change by CodecHelper#createVideoFormat
                      * {@link MediaCodecInfo.CodecProfileLevel}
                      */
                     internal var profile: Int = 0x08,
                     /**
                      * if support, default level 31. It will be change by CodecHelper#createVideoFormat
                      */
                     internal var level: Int = 0x200
    ) {
        companion object {
            const val HIGH = 5
            const val MEDIUM = 3
            const val LOW = 1
            const val BITRATE_MODE_CBR = 2
            const val BITRATE_MODE_CQ = 0
            const val BITRATE_MODE_VBR = 1
            fun create(): Video {
                return CodecContext.Video().apply {
                    mime = ""
                    width = -1
                    height = -1
                    bitrate = -1
                    iFrameInterval = -1
                    bitrateMode = -1
                    profile = -1
                    level = -1
                }
            }
        }
    }

    data class Audio(internal var mime: String = "audio/mp4a-latm",
                     internal var channel: Int = 1,
                     internal var sampleRateInHz: Int = 44100,
                     internal var bitrate: Int = sampleRateInHz * 2,
                     internal var profile: Int = MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                     internal var sampleBits: Int = AudioFormat.ENCODING_PCM_16BIT,
                     internal var deNoise: Boolean = false,
                     internal var silence: Boolean = false)

    enum class CodecType {
        HARD, SOFT
    }

    data class IOContext(var path: String? = null)
}