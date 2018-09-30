/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.presenter

import com.lmy.codec.texture.impl.filter.BaseFilter

/**
 * Created by lmyooyo@gmail.com on 2018/9/21.
 */
interface FilterSupport {
    fun setFilter(filter: BaseFilter)
    fun getFilter(): BaseFilter?
}