/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import com.lmy.codec.entity.Egl
import com.lmy.codec.helper.GLHelper
import com.lmy.codec.texture.impl.BaseTexture

/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
abstract class TextureWrapper(open var surfaceTexture: SurfaceTexture? = null,
                              var texture: BaseTexture? = null,
                              open var textureId: IntArray? = null,
                              var egl: Egl? = null) {

    abstract fun drawTexture(transformMatrix: FloatArray?)

    fun createTexture(target: Int): IntArray {
        val textureId = IntArray(1)
        GLES20.glGenTextures(1, textureId, 0)
        GLES20.glBindTexture(target, textureId[0])
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLHelper.checkGLES2Error("createTexture")
        return textureId
    }

    open fun release() {
        egl?.release()
        egl = null
        texture?.release()
        texture = null
        surfaceTexture?.release()
        surfaceTexture = null
        if (null != textureId)
            GLES20.glDeleteTextures(1, textureId, 0)
    }

    //更新xy坐标
    abstract fun updateLocation(srcWidth: Int, srcHeight: Int, destWidth: Int, destHeight: Int)

    //更新st坐标
    abstract fun updateTextureLocation(srcWidth: Int, srcHeight: Int, destWidth: Int, destHeight: Int)
}