package com.lmy.codec.entity

import android.media.MediaExtractor
import android.media.MediaFormat

data class Track(var index: Int,
                 var format: MediaFormat,
                 val extractor: MediaExtractor) {
    fun select() {
        extractor.selectTrack(index)
    }

    fun unselect() {
        extractor.unselectTrack(index)
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