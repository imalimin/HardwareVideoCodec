package com.lmy.codec.wrapper

import android.opengl.EGLContext
import android.view.Surface
import com.lmy.codec.entity.Egl
import com.lmy.codec.texture.impl.BaseTexture
import com.lmy.codec.util.debug_e

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class CodecTextureWrapper(surface: Surface? = null,
                          var eglContext: EGLContext? = null,
                          var egl: Egl? = null,
                          private var texture: BaseTexture? = null) : TextureWrapper() {
    init {
        if (null != surface) {
            egl = Egl()
            egl!!.initEGL(surface, null)
            egl!!.makeCurrent()
        } else {
            debug_e("Egl create failed")
        }
    }

    fun setFilter(texture: BaseTexture) {
        this.texture = texture
    }

    fun drawTexture(transformMatrix: FloatArray) {
        if (null == texture) {
            debug_e("Render failed. Texture is null")
            return
        }
        texture?.drawTexture(transformMatrix)
    }
}