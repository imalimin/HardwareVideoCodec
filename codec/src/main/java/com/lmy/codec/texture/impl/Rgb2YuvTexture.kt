package com.lmy.codec.texture.impl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.lmy.codec.helper.Resources

/**
 * Created by lmyooyo@gmail.com on 2018/8/10.
 */
class Rgb2YuvTexture(width: Int, height: Int,
                     textureId: IntArray) : BaseFrameBufferTexture(width, height, textureId) {

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
    private var aWidthLocation = 0
    private var aHeightLocation = 0

    init {
        init()
        createProgram()
        initFrameBuffer()
    }

    private fun createProgram() {
        shaderProgram = createProgram(Resources.instance.readAssetsAsString("shader/vertex_rgb2yuv.glsl"),
                Resources.instance.readAssetsAsString("shader/fragment_rgb2yuv.glsl"))
        aPositionLocation = getAttribLocation("aPosition")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        uTextureLocation = getUniformLocation("uTexture")
        aWidthLocation = getUniformLocation("width")
        aHeightLocation = getUniformLocation("height")
    }

    override fun draw(transformMatrix: FloatArray?) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
        GLES20.glUniform1i(uTextureLocation, 0)
        GLES20.glUniform1i(aWidthLocation, width)
        GLES20.glUniform1i(aHeightLocation, height)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)

        drawer.draw()

        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glFlush()
    }
}