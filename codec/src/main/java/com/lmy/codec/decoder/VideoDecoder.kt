/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.decoder

interface VideoDecoder : Decoder {
    companion object {
        const val KEY_ROTATION = "rotation-degrees"
    }

    fun delay(ns: Long)
    fun getWidth(): Int
    fun getHeight(): Int
}