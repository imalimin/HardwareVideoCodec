package com.lmy.codec.entity

import android.media.MediaExtractor
import android.media.MediaFormat

data class Track(var index: Int,
                 var format: MediaFormat) {
    fun select(extractor: MediaExtractor) {
        extractor.selectTrack(index)
    }

    companion object {
        fun getVideoTrack(extractor: MediaExtractor): Track? {
            return getTrack(extractor, "video")
        }

        fun getAudioTrack(extractor: MediaExtractor): Track? {
            return getTrack(extractor, "audio")
        }

        fun getTrack(extractor: MediaExtractor, type: String): Track? {
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime.startsWith(type))
                    return Track(i, format)
            }
            return null
        }
    }
}