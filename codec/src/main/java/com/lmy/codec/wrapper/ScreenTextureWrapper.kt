/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import android.opengl.EGLContext
import com.lmy.codec.entity.Egl
import com.lmy.codec.texture.impl.NormalTexture
import com.lmy.codec.util.debug_e


/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
class ScreenTextureWrapper(override var surfaceTexture: SurfaceTexture? = null,
                           override var textureId: Int?,
                           var eglContext: EGLContext? = null) : TextureWrapper() {

    init {
        if (null != surfaceTexture) {
            egl = Egl("Screen")
            egl!!.initEGL(surfaceTexture!!, eglContext)
            egl!!.makeCurrent()
            if (null == textureId)
                throw RuntimeException("textureId can not be null")
            texture = NormalTexture(textureId!!)
        } else {
            debug_e("Egl create failed")
        }
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        if (null == texture) {
            debug_e("Render failed. Texture is null")
            return
        }
        texture?.drawTexture(transformMatrix)
    }

    override fun updateLocation(srcWidth: Int, srcHeight: Int, destWidth: Int, destHeight: Int) {
        debug_e("($srcWidth, $srcHeight)($destWidth, $destHeight)")
        (texture as NormalTexture).updateLocation(destWidth / srcWidth.toFloat(),
                destHeight / srcHeight.toFloat())
    }

    override fun updateTextureLocation(srcWidth: Int, srcHeight: Int, destWidth: Int, destHeight: Int) {

    }
}