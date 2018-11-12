/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.presenter

interface VideoProcessor : Processor {
    fun save(path: String, startMs: Int, endMs: Int, end: Runnable?)
    fun setBitrate(bitrate: Int)
}