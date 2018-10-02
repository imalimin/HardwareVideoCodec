/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.pipeline

import android.os.Handler

/**
 * Created by lmyooyo@gmail.com on 2018/9/13.
 */
interface Pipeline {
    fun queueEvent(event: Runnable, front: Boolean = false)
    fun queueEvent(event: Runnable, delayed: Long)
    fun quit()
    fun started(): Boolean
    fun getName(): String
    fun getHandler(): Handler
    fun sleep()
    fun wake()
}