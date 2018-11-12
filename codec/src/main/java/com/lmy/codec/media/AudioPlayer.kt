/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.media

import android.media.AudioManager
import android.media.AudioTrack
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import java.nio.ByteBuffer

class AudioPlayer(val sampleRate: Int,
                  val channelConfig: Int,
                  val audioFormat: Int) {
    private var audioTrack: AudioTrack? = null

    init {
        debug_i("AudioPlayer sampleRate=$sampleRate, channelConfig=$channelConfig, " +
                "audioFormat=$audioFormat, minSize=${getMinBufferSize()}")
        audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, audioFormat,
                getMinBufferSize(), AudioTrack.MODE_STREAM)
        audioTrack?.play()
    }

    fun play(buffer: ByteBuffer, length: Int) {
        try {
            val data = ByteArray(length)
            buffer.get(data)
            audioTrack?.write(data, 0, length)
        } catch (e: Exception) {
            debug_e("Play error")
        }
    }

    fun release() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    private fun getMinBufferSize(): Int {
        return AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    }
}