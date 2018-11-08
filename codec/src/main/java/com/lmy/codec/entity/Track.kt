/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.entity

import android.media.MediaExtractor
import android.media.MediaFormat
import com.lmy.codec.entity.media.SequenceParameterSet
import com.lmy.codec.util.Debuger
import java.nio.ByteBuffer

data class Track(var index: Int,
                 var format: MediaFormat,
                 val extractor: MediaExtractor) {
    private var endUs: Long = -1
    private val startTime: Long
    private var isRecycle = false

    init {
        select()
        startTime = getSampleTime()
        unselect()
        SequenceParameterSet.from(format.getByteBuffer("csd-0"))
    }

    @Synchronized
    fun select() {
        if (isRecycle) return
        extractor.selectTrack(index)
    }

    @Synchronized
    fun unselect() {
        if (isRecycle) return
        extractor.unselectTrack(index)
    }

    @Synchronized
    fun readSampleData(byteBuf: ByteBuffer, offset: Int): Int {
        if (isRecycle) return -1
        if (endUs > 0 && getSampleTime() > endUs) return -1
        return extractor.readSampleData(byteBuf, 0)
    }

    @Synchronized
    fun advance() {
        if (isRecycle) return
        extractor.advance()
    }

    @Synchronized
    fun getSampleTime(): Long {
        if (isRecycle) return 0L
        return extractor.sampleTime
    }

    @Synchronized
    fun getSampleFlags(): Int {
        if (isRecycle) return 0
        return extractor.sampleFlags
    }

    @Synchronized
    fun seekTo(startUs: Long) {
        if (isRecycle) return
        extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
    }

    @Synchronized
    fun range(startUs: Long, endUs: Long) {
        if (isRecycle) return
        if (startUs >= endUs) {
            throw RuntimeException("endUs cannot smaller than startUs")
        }
        this.endUs = endUs
        seekTo(startUs)
    }

    fun getStartTime(): Long {
        return startTime
    }

    fun getDurationUs(): Long = format.getLong(MediaFormat.KEY_DURATION)

    @Synchronized
    internal fun release() {
        isRecycle = true
    }

    override fun toString(): String {
        if (!Debuger.Debug) return "Track(startTime=$startTime, format=$format)"
        return "Track(startTime=$startTime, format=$format), " +
                if (!format.containsKey("csd-0")) ""
                else "csd-0(${getBufferString(format.getByteBuffer("csd-0"))}), " +
                        if (!format.containsKey("csd-1")) ""
                        else "csd-1(${getBufferString(format.getByteBuffer("csd-1"))}), " +
                                if (!format.containsKey("csd-2")) ""
                                else "csd-2(${getBufferString(format.getByteBuffer("csd-2"))}), "
    }

    private fun getBufferString(buffer: ByteBuffer): String {
        val sb = StringBuffer()
        for (i in 0 until buffer.capacity()) {
            if (0 != i) sb.append(" ")
            var hex = Integer.toBinaryString(buffer.get(i).toInt())
            if (hex.length < 8) {
                for (j in 0 until 8 - hex.length) {
                    hex = "0$hex"
                }
            } else if (hex.length > 8) {
                hex = hex.substring(hex.length - 8, hex.length)
            }
            sb.append(hex)
        }
        return sb.toString()
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