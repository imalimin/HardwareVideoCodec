/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
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