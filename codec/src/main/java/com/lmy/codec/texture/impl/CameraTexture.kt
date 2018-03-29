package com.lmy.codec.texture.impl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/29.
 */
class CameraTexture(var inputTextureId: Int,
                    var camera2dVerticesBuffer: FloatBuffer? = null,
                    var drawIndecesBuffer: ShortBuffer? = null) : BaseFrameBufferTexture() {

    companion object {
        private var COORDS_PER_VERTEX = 2
        private var TEXTURE_COORDS_PER_VERTEX = 2
        private val CAMERA_VERTEX_SHADER = "" +
                "attribute vec4 aPosition;\n" +
                "attribute vec2 aTextureCoord;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main(){\n" +
                "    gl_Position= aPosition;\n" +
                "    vTextureCoord = aTextureCoord;\n" +
                "}"
        private val CAMERA_FRAGMENT_SHADER = "" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying mediump vec2 vTextureCoord;\n" +
                "uniform sampler2D uTexture;\n" +
                "void main(){\n" +
                "    vec4  color = texture2D(uTexture, vTextureCoord);\n" +
                "    gl_FragColor = color;\n" +
                "}"
        private val CAMERA2D_VERTEX_SHADER = "" +
                "attribute vec4 aPosition;\n" +
                "attribute vec4 aTextureCoord;\n" +
                "uniform mat4 uTextureMatrix;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main(){\n" +
                "    gl_Position= aPosition;\n" +
                "    vTextureCoord = (uTextureMatrix * aTextureCoord).xy;\n" +
                "}"
        private val CAMERA2D_FRAGMENT_SHADER = "" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying mediump vec2 vTextureCoord;\n" +
                "uniform samplerExternalOES uTexture;\n" +
                "void main(){\n" +
                "    vec4  color = texture2D(uTexture, vTextureCoord);\n" +
                "    gl_FragColor = color;\n" +
                "}"
        private val VERTEX_SHADER = "" +
                //顶点坐标
                "attribute vec4 aPosition;\n" +
                //纹理矩阵
                "uniform mat4 uTextureMatrix;\n" +
                //自己定义的纹理坐标
                "attribute vec4 aTextureCoord;\n" +
                //传给片段着色器的纹理坐标
                "varying vec2 vTextureCoord;\n" +
                "void main()\n" +
                "{\n" +
                //根据自己定义的纹理坐标和纹理矩阵求取传给片段着色器的纹理坐标
                "  vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;\n" +
                "  gl_Position = aPosition;\n" +
                "}\n"
        private val FRAGMENT_SHADER = "" +
                //使用外部纹理必须支持此扩展
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                //外部纹理采样器
                "uniform samplerExternalOES uTexture;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main() \n" +
                "{\n" +
                //获取此纹理（预览图像）对应坐标的颜色值
                "  vec4 vCameraColor = texture2D(uTexture, vTextureCoord);\n" +
                //输出颜色的RGB值
                "  gl_FragColor = vCameraColor;\n" +
                "}\n"
        private val DRAW_INDICES = shortArrayOf(0, 1, 2, 0, 2, 3)
        private val VERTICES = floatArrayOf(
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f)
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
//        drawIndecesBuffer = ByteBuffer.allocateDirect(2 * DRAW_INDICES.size).order(ByteOrder.nativeOrder()).asShortBuffer()
//        drawIndecesBuffer?.put(DRAW_INDICES)
//        drawIndecesBuffer?.position(0)
//
//        buffer = createShapeVerticesBuffer(VERTICES)
//        camera2dVerticesBuffer = createShapeVerticesBuffer(CAMERA_TEXTURE_VERTICES)
//        createCameraProgram()
        createCamera2dProgram()
        initFrameBuffer()
    }

    private fun createCameraProgram() {
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, CAMERA_VERTEX_SHADER)
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, CAMERA_FRAGMENT_SHADER)
        mShaderProgram = linkProgram(vertexShader!!, fragmentShader!!)
        mPositionLocation = getAttribLocation("aPosition")
        mTextureLocation = getAttribLocation("uTexture")
        mTextureCoordinateLocation = getAttribLocation("aTextureCoord")
    }

    private fun createCamera2dProgram() {
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        mShaderProgram = linkProgram(vertexShader!!, fragmentShader!!)
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        uTextureMatrix = getUniformLocation("uTextureMatrix")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        if (null == transformMatrix)
            throw RuntimeException("TransformMatrix can not be null")
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!!)
//        GLES20.glUseProgram(mShaderProgram!!)
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputTextureId)
//        GLES20.glUniform1i(uTextureLocation, 0)
//        enableVertex(aPositionLocation, aTextureCoordinateLocation, buffer!!, camera2dVerticesBuffer!!)
//        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0)
//        GLES20.glViewport(0, 0, 720, 1280)

//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawIndecesBuffer!!.limit(),
//                GLES20.GL_UNSIGNED_SHORT, drawIndecesBuffer)
//
//        GLES20.glFinish()
//        GLES20.glDisableVertexAttribArray(aPositionLocation)
//        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
//        GLES20.glUseProgram(0)
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputTextureId)
        GLES20.glUniform1i(uTextureLocation, 0)
        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0)
        GLES20.glViewport(0, 0, 720, 1280)

        if (null != buffer) {
            buffer!!.position(0)
            GLES20.glEnableVertexAttribArray(aPositionLocation)
            GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 16, buffer)

            buffer!!.position(2)
            GLES20.glEnableVertexAttribArray(aTextureCoordinateLocation)
            GLES20.glVertexAttribPointer(aTextureCoordinateLocation, 2, GLES20.GL_FLOAT, false, 16, buffer)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        }
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