/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.presenter

import android.view.TextureView
import java.io.File

/**
 * Created by lmyooyo@gmail.com on 2018/9/21.
 */
interface ImageProcessor : FilterSupport {
    fun prepare()
    fun setInputResource(file: File)
    fun setPreviewDisplay(view: TextureView)
    fun save(path: String)
    fun release()
}