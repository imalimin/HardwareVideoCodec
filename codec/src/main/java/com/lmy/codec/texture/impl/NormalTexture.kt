/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl

import android.opengl.GLES20
import com.lmy.codec.BaseApplication
import com.lmy.codec.helper.AssetsHelper

/**
 * 无滤镜效果
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class NormalTexture(textureId: Int) : BaseTexture(textureId) {

    companion object {
        private val VERTICES_SCREEN = floatArrayOf(
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f)
    }

    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0

    init {
        verticesBuffer = createShapeVerticesBuffer(VERTICES_SCREEN)
        createProgram()
    }

    private fun createProgram() {
        shaderProgram = createProgram(AssetsHelper.read(BaseApplication.assetManager(), "shader/vertex_normal.sh"),
                AssetsHelper.read(BaseApplication.assetManager(), "shader/fragment_normal.sh"))
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation, buffer!!, verticesBuffer!!)

        drawer.draw()

        GLES20.glFinish()
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)


//        GLES20.glUseProgram(mShaderProgram!!)
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureId)
//        GLES20.glUniform1i(uTextureLocation, 0)
//        drawer.draw()
////        if (null != transformMatrix)
////            GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0)
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