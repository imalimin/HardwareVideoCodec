/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.helper

import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities.createFromProfileLevel
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
        /**
         * MediaCodec兼容性问题：
         * 1. 部分7.0以上机型开启high效果不明显，如LG G6
         * 2. 部分机型开启high会导致BufferInfo.presentationTimeUs乱序，具体表现为0, 50000, 150000, 100000，如小米NOTE PRO
         * @param ignoreDevice 忽略设备兼容性检测
         */
        fun createVideoFormat(parameter: Parameter, ignoreDevice: Boolean = false): MediaFormat? {
            val codecInfo = getCodecInfo(parameter.video.mime, true)
            if (!ignoreDevice && null == codecInfo) {//Unsupport codec type
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
                if (!ignoreDevice)
                    setProfileLevel(parameter, codecInfo!!, mediaFormat)
            }
            return mediaFormat
        }

        fun createAudioFormat(parameter: Parameter, ignoreDevice: Boolean = false): MediaFormat? {
            val codecInfo = getCodecInfo(parameter.audio.mime, true)
            if (!ignoreDevice && null == codecInfo) {//Unsupport codec type
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

        private fun getCodecInfo(mime: String, isEncode: Boolean): MediaCodecInfo? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
                codecList.codecInfos
                        .filter { it.isEncoder == isEncode }
                        .forEach { info ->
                            info.supportedTypes.forEach {
                                loge(it)
                                if (it.equals(mime, ignoreCase = true)) {
                                    return info
                                }
                            }
                        }
            } else {
                val count = MediaCodecList.getCodecCount()
                (0 until count)
                        .map { MediaCodecList.getCodecInfoAt(it) }
                        .filter { it.isEncoder == isEncode }
                        .forEach { info ->
                            info.supportedTypes.forEach {
                                loge(it)
                                if (it.equals(mime, ignoreCase = true)) {
                                    return info
                                }
                            }
                        }
            }
            return null
        }

        private fun setProfileLevel(parameter: Parameter, codecInfo: MediaCodecInfo, format: MediaFormat) {
            //低于LOLLIPOP的系统不支持profile和level
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
            val mime = format.getString(MediaFormat.KEY_MIME)
            val profileLevels = codecInfo.getCapabilitiesForType(mime).profileLevels

            var selected = MediaCodecInfo.CodecProfileLevel()
            selected.profile = -1
            selected.level = -1
            //查找支持的最佳配置
            for (p in profileLevels) {
                loge("profile item: " + p.profile + ", " + p.level)
                if (supportsProfileLevel(p.profile, p.level, profileLevels, mime)) {
                    if (MediaCodecInfo.CodecProfileLevel.AVCProfileHigh == p.profile
                            && p.profile > selected.profile
                            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//AVCProfileHigh需要Android 7.0或以上才支持
                        selected = p
                    } else if (MediaCodecInfo.CodecProfileLevel.AVCProfileMain == p.profile
                            && p.profile > selected.profile) {
                        selected = p
                    }
                }
            }
            loge("selected: " + selected.profile + ", " + selected.level + ", " + supportsProfileLevel(selected.profile, selected.level, profileLevels, mime))
            //如果找不到则默认
            if (-1 == selected.profile) return
            setProfileLevel(parameter, format, selected.profile, selected.level)
        }

        private fun setProfileLevel(parameter: Parameter, format: MediaFormat, profile: Int, level: Int) {
            parameter.video.profile = profile
            parameter.video.level = level
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                format.setInteger(MediaFormat.KEY_PROFILE, parameter.video.profile)
            }
            //level must be set even below M
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                format.setInteger(MediaFormat.KEY_LEVEL, parameter.video.level)
            }
        }

        private fun supportsProfileLevel(profile: Int, level: Int?,
                                         profileLevels: Array<MediaCodecInfo.CodecProfileLevel>, mime: String): Boolean {
            for (pl in profileLevels) {
                if (pl.profile != profile) {
                    continue
                }

                // AAC does not use levels
                if (level == null || mime.equals(MediaFormat.MIMETYPE_AUDIO_AAC, ignoreCase = true)) {
                    return true
                }

                // H.263 levels are not completely ordered:
                // Level45 support only implies Level10 support
                if (mime.equals(MediaFormat.MIMETYPE_VIDEO_H263, ignoreCase = true)) {
                    if (pl.level != level && pl.level == MediaCodecInfo.CodecProfileLevel.H263Level45
                            && level > MediaCodecInfo.CodecProfileLevel.H263Level10) {
                        continue
                    }
                }

                // MPEG4 levels are not completely ordered:
                // Level1 support only implies Level0 (and not Level0b) support
                if (mime.equals(MediaFormat.MIMETYPE_VIDEO_MPEG4, ignoreCase = true)) {
                    if (pl.level != level && pl.level == MediaCodecInfo.CodecProfileLevel.MPEG4Level1
                            && level > MediaCodecInfo.CodecProfileLevel.MPEG4Level0) {
                        continue
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val HEVCHighTierLevels = (MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel1
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel2
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel21
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel3
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel31
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel4
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel41
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel5
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel51
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel52
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel6
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel61
                            or MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel62)
                    // HEVC levels incorporate both tiers and levels. Verify tier support.
                    if (mime.equals(MediaFormat.MIMETYPE_VIDEO_HEVC, ignoreCase = true)) {
                        val supportsHighTier = pl.level and HEVCHighTierLevels != 0
                        val checkingHighTier = level and HEVCHighTierLevels != 0
                        // high tier levels are only supported by other high tier levels
                        if (checkingHighTier && !supportsHighTier) {
                            continue
                        }
                    }

                    if (pl.level >= level) {
                        // if we recognize the listed profile/level, we must also recognize the
                        // profile/level arguments.
                        return if (createFromProfileLevel(mime, profile, pl.level) != null) {
                            createFromProfileLevel(mime, profile, level) != null
                        } else true
                    }
                }
            }
            return false
        }
    }
}