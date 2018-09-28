/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl

import android.opengl.GLES20
import com.lmy.codec.helper.Resources

/**
 * 无滤镜效果
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class NormalTexture(textureId: IntArray) : BaseTexture(textureId) {

    companion object {
        private val VERTICES_SCREEN = floatArrayOf(
                0.0f, 0.0f,//LEFT,BOTTOM
                1.0f, 0.0f,//RIGHT,BOTTOM
                0.0f, 1.0f,//LEFT,TOP
                1.0f, 1.0f//RIGHT,TOP
        )
    }

    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0

    init {
        createProgram()
    }

    private fun createProgram() {
        shaderProgram = createProgram(Resources.instance.readAssetsAsString("shader/vertex_normal.glsl"),
                Resources.instance.readAssetsAsString("shader/fragment_normal.glsl"))
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
    }

    override fun draw(transformMatrix: FloatArray?) {
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
        GLES20.glUniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)

        drawer.draw()

        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)
        GLES20.glFlush()
    }
}