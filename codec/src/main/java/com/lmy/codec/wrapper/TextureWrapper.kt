/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import com.lmy.codec.entity.Egl
import com.lmy.codec.texture.impl.BaseTexture

/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
abstract class TextureWrapper(open var surfaceTexture: SurfaceTexture? = null,
                              var texture: BaseTexture? = null,
                              open var textureId: Int? = null,
                              var egl: Egl? = null) {

    abstract fun drawTexture(transformMatrix: FloatArray?)

    open fun release() {
        egl?.release()
        if (null != surfaceTexture) {
            surfaceTexture!!.release()
            surfaceTexture = null
        }
    }
}