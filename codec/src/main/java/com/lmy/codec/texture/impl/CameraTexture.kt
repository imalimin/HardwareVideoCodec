/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.lmy.codec.BaseApplication
import com.lmy.codec.helper.AssetsHelper
import com.lmy.codec.util.debug_e

/**
 * Created by lmyooyo@gmail.com on 2018/3/29.
 */
class CameraTexture(width: Int, height: Int,
                    textureId: Int) : BaseFrameBufferTexture(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureMatrix = 0
    private val frameBufferLock = Any()
    private val verticesBufferLock = Any()

    init {
        verticesBuffer = createShapeVerticesBuffer(getVertices(1f, 1f))

        createProgram()
        initFrameBuffer()
    }

    fun updateFrameBuffer(width: Int, height: Int) {
        synchronized(frameBufferLock) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTexture!!)
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        }
    }

    fun crop(cropRatioWidth: Float, cropRatioHeight: Float) {
        synchronized(verticesBufferLock) {
            verticesBuffer = createShapeVerticesBuffer(getVertices(cropRatioWidth, cropRatioHeight))
        }
    }

    private fun createProgram() {
        shaderProgram = createProgram(AssetsHelper.read(BaseApplication.assetManager(), "shader/vertex_camera.sh"),
                AssetsHelper.read(BaseApplication.assetManager(), "shader/fragment_camera.sh"))
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        uTextureMatrix = getUniformLocation("uTextureMatrix")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        if (null == transformMatrix)
            throw RuntimeException("TransformMatrix can not be null")
        synchronized(frameBufferLock) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!!)
        }
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(uTextureLocation, 0)
        synchronized(verticesBufferLock) {
            enableVertex(aPositionLocation, aTextureCoordinateLocation, buffer!!, verticesBuffer!!)
        }
        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0)

        drawer.draw()

        GLES20.glFinish()
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
    }

    private fun getVertices(cropRatioWidth: Float, cropRatioHeight: Float): FloatArray {
        val x = if (cropRatioWidth > 1) 1f else cropRatioWidth
        val y = if (cropRatioHeight > 1) 1f else cropRatioHeight
        val left = (1 - x) / 2
        var right = left + x
        val bottom = (1 - y) / 2
        val top = bottom + y
        debug_e("crop($left, $top, $right, $bottom)")
        return floatArrayOf(
                left, bottom,//LEFT,BOTTOM
                right, bottom,//RIGHT,BOTTOM
                left, top,//LEFT,TOP
                right, top//RIGHT,TOP
        )
    }
}