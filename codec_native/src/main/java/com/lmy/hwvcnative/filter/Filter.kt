/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.hwvcnative.filter

import com.lmy.hwvcnative.CPPObject

abstract class Filter : CPPObject() {
    abstract fun setParams(params: IntArray)
}