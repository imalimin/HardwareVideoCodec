/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.decoder

import android.media.MediaExtractor
import android.media.MediaFormat
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
    }

    fun seekTo(startUs: Long) {
        videoTrack!!.seekTo(startUs)
        audioTrack?.seekTo(startUs)
    }

    fun range(startUs: Long, endUs: Long) {
        videoTrack!!.range(startUs, endUs)
        audioTrack?.range(startUs, endUs)
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
        videoExtractor.release()
        audioExtractor.release()
        videoTrack = null
        audioTrack = null
    }
}