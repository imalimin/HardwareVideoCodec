/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.wrapper

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import com.lmy.codec.entity.Egl
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.texture.impl.CameraTexture
import com.lmy.codec.util.debug_e


/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
class CameraTextureWrapper : TextureWrapper() {

    init {
        /**
         * 使用createTexture()会一直返回0，导致一些错误
         */
        textureId = 10
        intTexture()
    }

    fun initEGL(width: Int, height: Int) {
        egl = Egl()
        egl!!.initEGL()
        egl!!.makeCurrent()
        texture = CameraTexture(width, height, textureId!!)
        debug_e("camera textureId: $textureId")
    }

    @SuppressLint("Recycle")
    private fun intTexture() {
        if (null != textureId)
            surfaceTexture = SurfaceTexture(textureId!!)
    }

    private fun checkTexture() {
        if (null != texture && texture is BaseFrameBufferTexture) return
        throw RuntimeException("CameraTextureWrapper`s texture must be BaseFrameBufferTexture and texture must not be null")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        if (null == texture) {
            debug_e("Render failed. Texture is null")
            return
        }
        texture?.drawTexture(transformMatrix)
    }

    fun getFrameBuffer(): Int {
        checkTexture()
        return (texture as BaseFrameBufferTexture).frameBuffer!!
    }

    fun getFrameBufferTexture(): Int {
        checkTexture()
        return (texture as BaseFrameBufferTexture).frameBufferTexture!!
    }
}