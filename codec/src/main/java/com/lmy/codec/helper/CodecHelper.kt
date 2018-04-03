package com.lmy.codec.helper

import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import com.lmy.codec.entity.Parameter

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class CodecHelper {
    companion object {
        fun initFormat(mediaFormat: MediaFormat, parameter: Parameter) {
            mediaFormat.setString(MediaFormat.KEY_MIME, parameter.video.mime)
            mediaFormat.setInteger(MediaFormat.KEY_WIDTH, parameter.video.width)
            mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, parameter.video.height)
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, parameter.video.bitrate)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, parameter.video.fps)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, parameter.video.iFrameInterval)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, parameter.video.bitrateMode)
                mediaFormat.setInteger(MediaFormat.KEY_PROFILE, parameter.video.profile)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mediaFormat.setInteger(MediaFormat.KEY_LEVEL, parameter.video.level)
                }
            }
        }

        fun initAudioFormat(mediaFormat: MediaFormat, parameter: Parameter) {
            mediaFormat.setString(MediaFormat.KEY_MIME, parameter.audio.mime)
            mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, parameter.audio.channel)
            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, parameter.audio.sampleRateInHz)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, parameter.audio.bitrate)
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, parameter.audio.profile)
        }
    }
}