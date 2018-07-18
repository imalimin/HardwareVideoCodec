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

/**
 * Created by lmyooyo@gmail.com on 2018/3/29.
 */
class CameraTexture(width: Int, height: Int,
                    textureId: Int) : BaseFrameBufferTexture(width, height, textureId) {

    companion object {
        private val CAMERA_TEXTURE_VERTICES = floatArrayOf(
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f)
    }

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureMatrix = 0

    init {
        verticesBuffer = createShapeVerticesBuffer(CAMERA_TEXTURE_VERTICES)

        createProgram()
        initFrameBuffer()
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
            GLES20.glUseProgram(shaderProgram!!)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
            GLES20.glUniform1i(uTextureLocation, 0)
            enableVertex(aPositionLocation, aTextureCoordinateLocation, buffer!!, verticesBuffer!!)
            GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0)

            drawer.draw()

            GLES20.glFinish()
            GLES20.glDisableVertexAttribArray(aPositionLocation)
            GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NONE)
            GLES20.glUseProgram(GLES20.GL_NONE)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        }

        //simple
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!!)
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputTextureId)
//        GLES20.glUniform1i(uTextureLocation, 0)
//        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0)
//
//        if (null != buffer) {
//            buffer!!.position(0)
//            GLES20.glEnableVertexAttribArray(aPositionLocation)
//            GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 16, buffer)
//
//            buffer!!.position(2)
//            GLES20.glEnableVertexAttribArray(aTextureCoordinateLocation)
//            GLES20.glVertexAttribPointer(aTextureCoordinateLocation, 2, GLES20.GL_FLOAT, false, 16, buffer)
//
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
//        }
    }
}