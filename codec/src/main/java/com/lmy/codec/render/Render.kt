package com.lmy.codec.render

import android.graphics.SurfaceTexture

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
interface Render : SurfaceTexture.OnFrameAvailableListener {
    fun draw()
    fun start(texture: SurfaceTexture, width: Int, height: Int)
    fun stop()
    fun release()
}