/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.entity

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

data class Track(var index: Int,
                 var format: MediaFormat,
                 val extractor: MediaExtractor) {
    private var endUs: Long = -1
    @Synchronized
    fun select() {
        extractor.selectTrack(index)
    }

    @Synchronized
    fun unselect() {
        extractor.unselectTrack(index)
    }

    @Synchronized
    fun readSampleData(byteBuf: ByteBuffer, offset: Int): Int {
        if (endUs > 0 && getSampleTime() > endUs) return 0
        return extractor.readSampleData(byteBuf, 0)
    }

    @Synchronized
    fun advance() {
        extractor.advance()
    }

    fun getSampleTime(): Long {
        return extractor.sampleTime
    }

    fun getSampleFlags(): Int {
        return extractor.sampleFlags
    }

    @Synchronized
    fun seekTo(startUs: Long) {
        extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
    }

    @Synchronized
    fun range(startUs: Long, endUs: Long) {
        if (startUs >= endUs) {
            throw RuntimeException("endUs cannot smaller than startUs")
        }
        this.endUs = endUs
        seekTo(startUs)
    }

    companion object {
        fun getVideoTrack(extractor: MediaExtractor): Track? = getTrack(extractor, "video")

        fun getAudioTrack(extractor: MediaExtractor): Track? = getTrack(extractor, "audio")

        fun getTrack(extractor: MediaExtractor, type: String): Track? {
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime.startsWith(type))
                    return Track(i, format, extractor)
            }
            return null
        }
    }
}