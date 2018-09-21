/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.presenter

import android.view.TextureView
import com.lmy.codec.texture.impl.filter.BaseFilter
import java.io.File

/**
 * Created by lmyooyo@gmail.com on 2018/9/21.
 */
interface ImageProcessor {
    fun prepare()
    fun setInputImage(file: File)
    fun setPreviewDisplay(view: TextureView)
    fun setFilter(filter: Class<*>)
    fun getFilter(): BaseFilter?
    fun release()
}