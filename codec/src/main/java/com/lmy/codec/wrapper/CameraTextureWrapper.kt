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
import com.lmy.codec.entity.CodecContext
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
        texture = CameraTexture(width, height, textureId!!).apply {
            name = "Camera Texture"
        }
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

    override fun updateLocation(context: CodecContext) {
        (texture as CameraTexture).updateFrameBuffer(context.video.width, context.video.height)
        val location = FloatArray(8)
        val textureLocation = FloatArray(8)
        calculateBestLocation(context, location, textureLocation)
        texture?.updateLocation(textureLocation, location)
    }

    private fun calculateBestLocation(context: CodecContext,
                                      location: FloatArray, textureLocation: FloatArray) {
        val previewWidth = context.cameraSize.height
        val previewHeight = context.cameraSize.width
        val videoWidth = context.video.width
        val videoHeight = context.video.height
        val previewScale = previewWidth / previewHeight.toFloat()
        val videoScale = videoWidth / videoHeight.toFloat()
        var destPreviewWidth = previewWidth
        var destPreviewHeight = previewHeight
        /**
         * if (previewScale > videoScale) previewHeight不变，以previewHeight为准计算previewWidth
         * else previewWidth不变，以previewWidth为准计算previewHeight
         */
        if (previewScale > videoScale) {
            destPreviewWidth = (previewHeight * videoScale).toInt()
            if (0 != destPreviewWidth % 2) ++destPreviewWidth
        } else {
            destPreviewHeight = (previewWidth / videoScale).toInt()
            if (0 != destPreviewHeight % 2) ++destPreviewHeight
        }
        val left = (previewWidth - destPreviewWidth) / 2f / previewWidth.toFloat()
        val right = 1f - left
        val bottom = (previewHeight - destPreviewHeight) / 2f / previewHeight.toFloat()
        val top = 1 - bottom
        //顶点位置
        System.arraycopy(floatArrayOf(-1f, -1f, //LEFT,BOTTOM
                1f, -1f, //RIGHT,BOTTOM
                -1f, 1f, //LEFT,TOP
                1f, 1f//RIGHT,TOP
        ), 0, location, 0, 8)
        //顶点纹理坐标
        System.arraycopy(floatArrayOf(left, bottom, //LEFT,BOTTOM
                right, bottom, //RIGHT,BOTTOM
                left, top, //LEFT,TOP
                right, top//RIGHT,TOP
        ), 0, textureLocation, 0, 8)
    }
}