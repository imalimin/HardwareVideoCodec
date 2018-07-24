/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.wrapper

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import com.lmy.codec.entity.Egl
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.texture.impl.CameraTexture
import com.lmy.codec.util.debug_e


/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
class CameraTextureWrapper(width: Int,
                           height: Int) : TextureWrapper() {

    init {
        egl = Egl("Camera")
        egl!!.initEGL()
        egl!!.makeCurrent()
        textureId = createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
        texture = CameraTexture(width, height, textureId!!)
        intTexture()
    }

    @SuppressLint("Recycle")
    private fun intTexture() {
        if (null != textureId)
            surfaceTexture = SurfaceTexture(textureId!![0])
        debug_e("camera textureId: ${textureId!![0]}")
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

    fun getFrameBuffer(): IntArray {
        checkTexture()
        return (texture as BaseFrameBufferTexture).frameBuffer
    }

    fun getFrameBufferTexture(): IntArray {
        checkTexture()
        return (texture as BaseFrameBufferTexture).frameBufferTexture
    }

    override fun updateLocation(srcWidth: Int, srcHeight: Int, destWidth: Int, destHeight: Int) {

    }

    override fun updateTextureLocation(srcWidth: Int, srcHeight: Int, destWidth: Int, destHeight: Int) {
        (texture as CameraTexture).updateFrameBuffer(destWidth, destHeight)
        (texture as CameraTexture).updateTextureLocation(destWidth / srcHeight.toFloat(),
                destHeight / srcWidth.toFloat())
    }
}