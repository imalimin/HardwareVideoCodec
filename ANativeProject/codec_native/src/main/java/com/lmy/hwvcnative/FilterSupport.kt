/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.hwvcnative

import com.lmy.hwvcnative.filter.Filter

interface FilterSupport {
    fun setFilter(filter: Filter)
    fun getFilter(): Filter?
    fun invalidate()
}