package com.lmy.codec

import android.graphics.SurfaceTexture
import com.lmy.codec.entity.Parameter

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
interface IRecorder {
    fun prepare(param: Parameter)
    fun startPreview(surface: SurfaceTexture, width: Int, height: Int)
    fun updatePreview(width: Int, height: Int)
    fun stopPreview()
    fun release()
}