package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import com.lmy.codec.texture.Texture

/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
open class TextureWrapper(open var surfaceTexture: SurfaceTexture? = null,
                          open var textureId: Int? = null,
                          open var texture: Texture? = null) {

    open fun release() {
        if (null != surfaceTexture) {
            surfaceTexture!!.release()
            surfaceTexture = null
        }
    }
}