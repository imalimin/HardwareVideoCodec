/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.hwvcnative.filter

class BeautyV4Filter : Filter() {

    init {
        handler = create()
    }

    override fun setParams(params: IntArray) {
        if (0L == handler) return
        setParams(handler, params)
    }

    private external fun create(): Long
    private external fun setParams(handler: Long, params: IntArray)
}