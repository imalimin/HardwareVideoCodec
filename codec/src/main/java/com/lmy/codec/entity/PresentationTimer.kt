/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.entity

/**
 * Created by lmyooyo@gmail.com on 2018/9/13.
 */
class PresentationTimer(var fps: Int,
                        var presentationTimeUs: Long = 0,
                        private var timestamp: Long = 0) {

    fun start() {
        timestamp = 0
    }

    fun record() {
        val timeTmp = System.nanoTime() / 1000
        presentationTimeUs += if (0L != timestamp)
            timeTmp - timestamp
        else
            1000000L / fps
        timestamp = timeTmp
    }

    fun reset() {
        presentationTimeUs = 0
        timestamp = 0
    }
}