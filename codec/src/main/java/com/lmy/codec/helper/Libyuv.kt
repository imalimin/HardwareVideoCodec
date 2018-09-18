/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.helper

object Libyuv {
    init {
        System.loadLibrary("codec")
    }

    const val kRotate0 = 0
    const val kRotate90 = 1
    const val kRotate180 = 2
    const val kRotate270 = 3
    const val COLOR_RGBA = 0x0100
    const val COLOR_I420 = 0x0001
    external fun ConvertToI420(src: ByteArray, dest: ByteArray, width: Int, height: Int, rotation: Int): Boolean
}