/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class SepiaFilter(width: Int = 0,
                  height: Int = 0,
                  textureId: IntArray = IntArray(1)) : ColorMatrixFilter(width, height, textureId, 0f, MATRIX) {
    companion object {
        val MATRIX: FloatArray = floatArrayOf(
                0.3588f, 0.7044f, 0.1368f, 0.0f,
                0.2990f, 0.5870f, 0.1140f, 0.0f,
                0.2392f, 0.4696f, 0.0912f, 0.0f,
                0f, 0f, 0f, 1.0f)
    }
}