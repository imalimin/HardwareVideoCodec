/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.presenter

import android.view.TextureView

interface VideoPlayer : FilterSupport {
    var onPlayStateListener: OnPlayStateListener?
    fun seekTo(timeUs: Long)
    fun reset()
    fun prepare()
    fun setInputResource(path: String)
    fun setPreviewDisplay(view: TextureView)
    fun isPlaying(): Boolean
    fun start()
    fun pause()
    fun stop()
    fun release()
    interface OnPlayStateListener {
        fun onPrepared(play: VideoPlayer, durationUs: Long)
        fun onStart(play: VideoPlayer)
        fun onPause(play: VideoPlayer)
        fun onStop(play: VideoPlayer)
        fun onPlaying(play: VideoPlayer, timeUs: Long, durationUs: Long)
    }
}