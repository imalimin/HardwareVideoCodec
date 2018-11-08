/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.decoder

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Track
import com.lmy.codec.util.debug_e
import java.io.IOException

/**
 * Created by lmyooyo@gmail.com on 2018/10/15.
 */
class VideoExtractor(private val context: CodecContext,
                     private val inputPath: String) {
    private val videoExtractor = MediaExtractor()
    private val audioExtractor = MediaExtractor()
    private var videoTrack: Track? = null
    private var audioTrack: Track? = null

    init {
        prepareExtractor()
    }

    private fun prepareExtractor() {
        try {
            videoExtractor.setDataSource(this.inputPath)
            audioExtractor.setDataSource(this.inputPath)
        } catch (e: IOException) {
            debug_e("File(${context.ioContext.path}) not found")
            return
        }
        videoTrack = Track.getVideoTrack(videoExtractor)
        audioTrack = Track.getAudioTrack(audioExtractor)
        context.audio.silence = !containAudio()//是否有音频
        videoTrack!!.select()
        audioTrack?.select()
        context.video.fps = getVideoTrack()!!.format.getInteger(MediaFormat.KEY_FRAME_RATE)
        context.orientation = if (videoTrack!!.format.containsKey(VideoDecoder.KEY_ROTATION))
            videoTrack!!.format.getInteger(VideoDecoder.KEY_ROTATION) else 0
        if (context.isHorizontal()) {
            context.video.width = videoTrack!!.format.getInteger(MediaFormat.KEY_WIDTH)
            context.video.height = videoTrack!!.format.getInteger(MediaFormat.KEY_HEIGHT)
            context.cameraSize.width = context.video.width
            context.cameraSize.height = context.video.height
        } else {
            context.video.width = videoTrack!!.format.getInteger(MediaFormat.KEY_HEIGHT)
            context.video.height = videoTrack!!.format.getInteger(MediaFormat.KEY_WIDTH)
            context.cameraSize.width = context.video.height
            context.cameraSize.height = context.video.width
        }
        if (getVideoTrack()!!.format.containsKey(MediaFormat.KEY_PROFILE)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.video.profile = getVideoTrack()!!.format.getInteger(MediaFormat.KEY_PROFILE)
        }
        if (getVideoTrack()!!.format.containsKey(MediaFormat.KEY_LEVEL)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.video.level = getVideoTrack()!!.format.getInteger(MediaFormat.KEY_LEVEL)
        }
        if (getVideoTrack()!!.format.containsKey(MediaFormat.KEY_BIT_RATE)) {
            context.video.bitrateMode = CodecContext.Video.BITRATE_MODE_CBR//静态码率
        } else {
            context.video.bitrateMode = CodecContext.Video.BITRATE_MODE_VBR//动态码率
        }
    }

    fun seekTo(timeUs: Long) {
        videoTrack!!.seekTo(timeUs)
        audioTrack?.seekTo(videoTrack!!.getSampleTime())
    }

    fun range(startUs: Long, endUs: Long) {
        videoTrack!!.range(startUs, endUs)
        audioTrack?.range(videoTrack!!.getSampleTime(), endUs)
    }

    fun getVideoTrack(): Track? {
        return videoTrack
    }

    fun getAudioTrack(): Track? {
        return audioTrack
    }

    fun containAudio(): Boolean {
        return null != audioTrack
    }

    fun release() {
        videoTrack?.release()
        videoTrack = null
        audioTrack?.release()
        audioTrack = null
        videoExtractor.release()
        audioExtractor.release()
    }
}