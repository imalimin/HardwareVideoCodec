package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import com.lmy.codec.entity.Egl

/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
abstract class TextureWrapper(open var surfaceTexture: SurfaceTexture? = null,
                          open var textureId: Int? = null,
                              var egl: Egl? = null) {

    abstract fun drawTexture(transformMatrix: FloatArray?)

    open fun release() {
        if (null != surfaceTexture) {
            surfaceTexture!!.release()
            surfaceTexture = null
        }
    }
}