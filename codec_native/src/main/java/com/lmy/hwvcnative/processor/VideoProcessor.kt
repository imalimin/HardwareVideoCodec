/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
package com.lmy.hwvcnative.processor

import android.view.Surface
import com.lmy.hwvcnative.CPPObject
import com.lmy.hwvcnative.FilterSupport
import com.lmy.hwvcnative.filter.Filter

class VideoProcessor : CPPObject(), FilterSupport {
    private var filter: Filter? = null

    init {
        handler = create()
    }

    override fun setFilter(filter: Filter) {

    }

    override fun getFilter(): Filter? {
        return filter
    }

    override fun invalidate() {
    }

    fun prepare(surface: Surface, width: Int, height: Int) {
        if (0L == handler) return
        prepare(handler, surface, width, height)
    }

    fun start() {
        if (0L == handler) return
        start(handler)
    }

    fun pause() {
        if (0L == handler) return
        pause(handler)
    }

    fun release() {
        release(handler)
        handler = 0
    }

    private external fun create(): Long
    private external fun prepare(handler: Long, surface: Surface, width: Int, height: Int)
    private external fun start(handler: Long)
    private external fun pause(handler: Long)
    private external fun release(handler: Long)
}