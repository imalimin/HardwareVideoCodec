/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.native.processor

import com.lmy.codec.util.debug_i

class DeNoise(sampleRate: Int, sampleSize: Int) {
    init {
        System.loadLibrary("speexdsp")
        debug_i("Setup DeNoise, sampleRate=$sampleRate, sampleSize=$sampleSize")
        start(sampleRate, sampleSize)
    }

    private external fun start(sampleRate: Int, sampleSize: Int)
    external fun preprocess(data: ByteArray): Int
    external fun stop()
}