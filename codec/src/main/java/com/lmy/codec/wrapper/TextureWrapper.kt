package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture

/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
open class TextureWrapper(open var surfaceTexture: SurfaceTexture? = null,
                          open var textureId: Int? = null) {

    open fun release() {
        if (null != surfaceTexture) {
            surfaceTexture!!.release()
            surfaceTexture = null
        }
    }
}