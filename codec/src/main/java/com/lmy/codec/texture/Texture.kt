/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
interface Texture {
    fun draw(transformMatrix: FloatArray?)
}