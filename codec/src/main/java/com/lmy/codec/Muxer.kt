package com.lmy.codec

import android.media.MediaFormat
import com.lmy.codec.entity.Sample

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
interface Muxer {
    fun addVideoTrack(format: MediaFormat)
    fun addAudioTrack(format: MediaFormat)
    fun writeVideoSample(sample: Sample)
    fun writeAudioSample(sample: Sample)
    fun release()
}