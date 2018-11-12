/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.egl

import android.graphics.SurfaceTexture
import android.opengl.EGLContext
import com.lmy.codec.egl.entity.Egl
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.texture.impl.NormalTexture
import com.lmy.codec.util.debug_e


/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
class ScreenEglSurface private constructor(eglContext: EGLContext?,
                                           surface: SurfaceTexture,
                                           textureId: IntArray?) : EglOutputSurface() {

    init {
        this.surface = surface
        this.textureId = textureId
        egl = Egl("Screen")
        egl!!.initEGL(surface, eglContext)
        egl!!.makeCurrent()
        if (null == textureId)
            throw RuntimeException("textureId can not be null")
        texture = NormalTexture(textureId!!).apply {
            name = "Screen Texture"
        }
    }

    override fun draw(transformMatrix: FloatArray?) {
        if (null == texture) {
            debug_e("Render failed. Texture is null")
            return
        }
        texture?.draw(transformMatrix)
    }

    override fun updateLocation(context: CodecContext) {
        val location = FloatArray(8)
        val textureLocation = FloatArray(8)
        calculateLocation(context, location, textureLocation)
        texture?.updateLocation(textureLocation, location)
    }

    private fun calculateLocation(context: CodecContext,
                                  location: FloatArray, textureLocation: FloatArray) {
        val viewWidth = context.viewSize.width
        val viewHeight = context.viewSize.height
        val viewScale = viewWidth / viewHeight.toFloat()
        val videoScale = context.video.width / context.video.height.toFloat()
        var destViewWidth = viewWidth
        var destViewHeight = viewHeight
        if (viewScale > videoScale) {
            destViewWidth = (viewHeight * videoScale).toInt()
        } else {
            destViewHeight = (viewWidth / videoScale).toInt()
        }
        val left = -destViewWidth / viewWidth.toFloat()
        val right = -left
        val bottom = -destViewHeight / viewHeight.toFloat()
        val top = -bottom
        System.arraycopy(floatArrayOf(left, bottom, //LEFT,BOTTOM
                right, bottom, //RIGHT,BOTTOM
                left, top, //LEFT,TOP
                right, top//RIGHT,TOP
        ), 0, location, 0, 8)
        System.arraycopy(floatArrayOf(0f, 0f, //LEFT,BOTTOM
                1f, 0f, //RIGHT,BOTTOM
                0f, 1f, //LEFT,TOP
                1f, 1f//RIGHT,TOP
        ), 0, textureLocation, 0, 8)
    }

    companion object {
        fun create(surface: SurfaceTexture, textureId: IntArray?,
                   eglContext: EGLContext?): ScreenEglSurface = ScreenEglSurface(eglContext, surface, textureId)
    }
}