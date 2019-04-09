/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.hwvcnative.filter

class PinkFilter(names: Array<String>,
                 samplers: Array<String>) : Filter() {

    init {
        handler = create(names, samplers)
    }

    override fun setParams(params: IntArray) {
        if (0L == handler) return
        setParams(handler, params)
    }

    override fun setParam(index: Int, value: Int) {

    }

    private external fun create(names: Array<String>, samplers: Array<String>): Long
    private external fun setParams(handler: Long, params: IntArray)
}