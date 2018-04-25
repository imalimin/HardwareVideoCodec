/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.render

import android.graphics.SurfaceTexture
import com.lmy.codec.texture.impl.BaseFrameBufferTexture

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
interface Render {
    fun onFrameAvailable(): Render
    fun draw()
    fun start(texture: SurfaceTexture, width: Int, height: Int)
    fun start(texture: SurfaceTexture, width: Int, height: Int, runnable: Runnable?)
    fun stop()
    fun release()
    /**
     * After render completed
     */
    fun afterRender(runnable: Runnable)

    fun setFilter(filter: BaseFrameBufferTexture)
    fun getFrameBuffer(): Int
    fun getFrameBufferTexture(): Int
}