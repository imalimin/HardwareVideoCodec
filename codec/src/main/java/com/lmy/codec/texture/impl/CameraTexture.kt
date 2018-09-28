/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.lmy.codec.helper.Resources

/**
 * Created by lmyooyo@gmail.com on 2018/3/29.
 */
class CameraTexture(width: Int, height: Int,
                    textureId: IntArray) : BaseFrameBufferTexture(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureMatrix = 0

    init {
        name = "CameraTexture"
        createProgram()
        initFrameBuffer()
    }

    private fun createProgram() {
        shaderProgram = createProgram(Resources.instance.readAssetsAsString("shader/vertex_camera.glsl"),
                Resources.instance.readAssetsAsString("shader/fragment_camera.glsl"))
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        uTextureMatrix = getUniformLocation("uTextureMatrix")
    }

    override fun draw(transformMatrix: FloatArray?) {
        if (null == transformMatrix)
            throw RuntimeException("TransformMatrix can not be null")
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId[0])
        GLES20.glUniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0)

        drawer.draw()

        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glFlush()
    }
}