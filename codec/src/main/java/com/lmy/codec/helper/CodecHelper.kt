package com.lmy.codec.helper

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import com.lmy.codec.entity.Parameter
import com.lmy.codec.loge


/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class CodecHelper {
    companion object {
        fun createVideoFormat(parameter: Parameter, ignoreDevice: Boolean = false): MediaFormat? {
            if (!ignoreDevice && !isSupportCodec(parameter.video.mime, true)) {//Unsupport codec type
                return null
            }
            val mediaFormat = MediaFormat()
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
            return mediaFormat
        }

        fun createAudioFormat(parameter: Parameter, ignoreDevice: Boolean = false): MediaFormat? {
            if (!ignoreDevice && !isSupportCodec(parameter.audio.mime, true)) {//Unsupport codec type
                return null
            }
            val mediaFormat = MediaFormat()
            mediaFormat.setString(MediaFormat.KEY_MIME, parameter.audio.mime)
            mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, parameter.audio.channel)
            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, parameter.audio.sampleRateInHz)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, parameter.audio.bitrate)
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, parameter.audio.profile)
            return mediaFormat
        }

        private fun isSupportCodec(mime: String, isEncode: Boolean): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
                val codecInfos = codecList.codecInfos
                codecInfos.forEach {
                    if (it.isEncoder == isEncode) {
                        val types = it.supportedTypes
                        types.forEach {
                            loge(it)
                            if (it.equals(mime, ignoreCase = true)) {
                                return true
                            }
                        }
                    }
                }
            } else {
                val count = MediaCodecList.getCodecCount()
                (0 until count)
                        .map { MediaCodecList.getCodecInfoAt(it) }
                        .filter { it.isEncoder == isEncode }
                        .map { it.supportedTypes }
                        .forEach { types ->
                            types.forEach {
                                loge(it)
                                if (it.equals(mime, ignoreCase = true)) {
                                    return true
                                }
                            }
                        }
            }
            return false
        }
    }
}