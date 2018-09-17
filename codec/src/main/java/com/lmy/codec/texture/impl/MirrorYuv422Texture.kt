package com.lmy.codec.texture.impl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.lmy.codec.helper.Resources

/**
 * Created by lmyooyo@gmail.com on 2018/9/10.
 */
class MirrorYuv422Texture(width: Int, height: Int,
                          textureId: IntArray,
                          private var direction: Direction = Direction.VERTICAL)
    : BaseFrameBufferTexture(width, height, textureId) {
    enum class Direction {
        VERTICAL, HORIZONTAL
    }

    companion object {
        private val VERTICES_VERTICAL = floatArrayOf(
                0.0f, 1.0f,//LEFT,TOP
                1.0f, 1.0f,//RIGHT,TOP
                0.0f, 0.0f,//LEFT,BOTTOM
                1.0f, 0.0f//RIGHT,BOTTOM
        )
        private val VERTICES_HORIZONTAL = floatArrayOf(
                1.0f, 0.0f,//RIGHT,BOTTOM
                0.0f, 0.0f,//LEFT,BOTTOM
                1.0f, 1.0f,//RIGHT,TOP
                0.0f, 1.0f//LEFT,TOP
        )
    }

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var widthLocation = 0

    init {
        updateLocation(if (MirrorTexture.Direction.VERTICAL == direction) VERTICES_VERTICAL
        else VERTICES_HORIZONTAL, floatArrayOf(
                -1f, -1f,//LEFT,BOTTOM
                1f, -1f,//RIGHT,BOTTOM
                -1f, 1f,//LEFT,TOP
                1f, 1f//RIGHT,TOP
        ))
        createProgram()
        initFrameBuffer()
    }

    private fun createProgram() {
        shaderProgram = createProgram(Resources.instance.readAssetsAsString("shader/vertex_mirror.sh"),
                Resources.instance.readAssetsAsString("shader/fragment_rgba_to_yuv422.sh"))
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        widthLocation = getAttribLocation("width")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
        GLES20.glUniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        setUniform1f(widthLocation, width.toFloat())
        drawer.draw()

        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glFlush()
    }
}