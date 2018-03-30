package com.lmy.codec.texture.impl

import android.opengl.GLES20
import java.nio.FloatBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class NormalTexture(var inputTextureId: Int,
                    var drawer: CameraTexture.GLDrawer) : BaseTexture() {

    companion object {
        private val VERTEX_SHADER = "" +
                "attribute vec4 aPosition;\n" +
                "attribute vec2 aTextureCoord;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main(){\n" +
                "    gl_Position= aPosition;\n" +
                "    vTextureCoord = aTextureCoord;\n" +
                "}"
        private val FRAGMENT_SHADER = "" +
                "precision mediump float;\n" +
                "varying mediump vec2 vTextureCoord;\n" +
                "uniform sampler2D uTexture;\n" +
                "void main(){\n" +
                "    vec4  color = texture2D(uTexture, vTextureCoord);\n" +
                "    gl_FragColor = color;\n" +
                "}"
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
        shaderProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureId)
        GLES20.glUniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation, buffer!!, verticesBuffer!!)

        drawer.draw()

        GLES20.glFinish()
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)


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

    fun enableVertex(posLoc: Int, texLoc: Int, shapeBuffer: FloatBuffer, texBuffer: FloatBuffer) {
        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glEnableVertexAttribArray(texLoc)
        GLES20.glVertexAttribPointer(posLoc, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                COORDS_PER_VERTEX * 4, shapeBuffer)
        GLES20.glVertexAttribPointer(texLoc, TEXTURE_COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                TEXTURE_COORDS_PER_VERTEX * 4, texBuffer)
    }
}