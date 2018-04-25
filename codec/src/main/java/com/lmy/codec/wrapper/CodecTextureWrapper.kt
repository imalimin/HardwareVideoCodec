/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.wrapper

import android.opengl.EGLContext
import android.view.Surface
import com.lmy.codec.entity.Egl
import com.lmy.codec.texture.impl.BaseTexture
import com.lmy.codec.util.debug_e

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class CodecTextureWrapper(var surface: Surface,
                          var eglContext: EGLContext? = null) : TextureWrapper() {
    init {
        egl = Egl()
        egl!!.initEGL(surface, eglContext)
        egl!!.makeCurrent()
    }

    fun setFilter(texture: BaseTexture) {
        this.texture = texture
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        if (null == texture) {
            debug_e("Render failed. Texture is null")
            return
        }
        texture?.drawTexture(transformMatrix)
    }

    override fun release() {
        super.release()
        surface.release()
    }
}