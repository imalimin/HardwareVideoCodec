package com.lmy.codec.texture.impl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/29.
 */
class CameraTexture(width: Int, height: Int,
                    var inputTextureId: Int,
                    var drawer: GLDrawer = GLDrawer()) : BaseFrameBufferTexture(width, height) {

    companion object {
        private val VERTEX_SHADER = "" +
                "attribute vec4 aPosition;\n" +
                "attribute vec4 aTextureCoord;\n" +
                "uniform mat4 uTextureMatrix;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main(){\n" +
                "    gl_Position= aPosition;\n" +
                "    vTextureCoord = (uTextureMatrix * aTextureCoord).xy;\n" +
                "}"
        private val FRAGMENT_SHADER = "" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying mediump vec2 vTextureCoord;\n" +
                "uniform samplerExternalOES uTexture;\n" +
                "void main(){\n" +
                "    vec4  color = texture2D(uTexture, vTextureCoord);\n" +
                "    gl_FragColor = color;\n" +
                "}"
        private val DRAW_INDICES = shortArrayOf(0, 1, 2, 0, 2, 3)
        private val CAMERA_TEXTURE_VERTICES = floatArrayOf(
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f)
    }

    private var mPositionLocation = 0
    private var mTextureLocation = 0
    private var mTextureCoordinateLocation = 0

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
        shaderProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        uTextureMatrix = getUniformLocation("uTextureMatrix")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        if (null == transformMatrix)
            throw RuntimeException("TransformMatrix can not be null")
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!!)
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputTextureId)
        GLES20.glUniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation, buffer!!, verticesBuffer!!)
        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0)

        drawer.draw()

        GLES20.glFinish()
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        GLES20.glUseProgram(0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

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

    class GLDrawer(var drawIndecesBuffer: ShortBuffer? = null) {
        init {
            drawIndecesBuffer = ByteBuffer.allocateDirect(2 * DRAW_INDICES.size).order(ByteOrder.nativeOrder()).asShortBuffer()
            drawIndecesBuffer?.put(DRAW_INDICES)
            drawIndecesBuffer?.position(0)
        }

        fun draw() {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawIndecesBuffer!!.limit(),
                    GLES20.GL_UNSIGNED_SHORT, drawIndecesBuffer)
        }
    }
}