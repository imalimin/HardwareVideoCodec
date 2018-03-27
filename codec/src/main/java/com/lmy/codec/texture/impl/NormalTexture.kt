package com.lmy.codec.texture.impl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class NormalTexture(var inputTextureId: Int) : BaseTexture() {

    companion object {
        private val VERTEX_SHADER = "" +
                //顶点坐标
                "attribute vec4 aPosition;\n" +
                //纹理矩阵
                "uniform mat4 uTextureMatrix;\n" +
                //自己定义的纹理坐标
                "attribute vec4 aTextureCoordinate;\n" +
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
                "uniform samplerExternalOES uTextureSampler;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main() \n" +
                "{\n" +
                //获取此纹理（预览图像）对应坐标的颜色值
                "  vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);\n" +
                //输出颜色的RGB值
                "  gl_FragColor = vec4(vCameraColor.r, vCameraColor.g, vCameraColor.b, 1.0);\n" +
                "}\n"
        //每行前两个值为顶点坐标，后两个为纹理坐标
        private val VERTEX_DATA = floatArrayOf(1f, 1f, 1f, 1f, -1f, 1f, 0f, 1f, -1f, -1f, 0f, 0f, 1f, 1f, 1f, 1f, -1f, -1f, 0f, 0f, 1f, -1f, 1f, 0f)
    }

    private var aPositionLocation = 0
    private var aTextureCoordLocation = 0
    private var uTextureMatrixLocation = 0
    private var uTextureSamplerLocation = 0

    init {
        initBuffer()
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        mShaderProgram = linkProgram(vertexShader!!, fragmentShader!!)
    }

    private fun initBuffer() {
        buffer = ByteBuffer.allocateDirect(VERTEX_DATA.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        buffer!!.put(VERTEX_DATA, 0, VERTEX_DATA.size).position(0)
    }

    override fun drawTexture(transformMatrix: FloatArray) {
        aPositionLocation = getPositionLocation()
        aTextureCoordLocation = getTextureCoordinateLocation()
        uTextureMatrixLocation = getTextureMatrixLocation()
        uTextureSamplerLocation = getTextureSamplerLocation()

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputTextureId)
        GLES20.glUniform1i(uTextureSamplerLocation, 0)
        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0)

        if (null != buffer) {
            buffer!!.position(0)
            GLES20.glEnableVertexAttribArray(aPositionLocation)
            GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 16, buffer)

            buffer!!.position(2)
            GLES20.glEnableVertexAttribArray(aTextureCoordLocation)
            GLES20.glVertexAttribPointer(aTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 16, buffer)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        }
    }

    fun getPositionLocation(): Int {
        return getAttribLocation("aPosition")
    }

    fun getTextureCoordinateLocation(): Int {
        return getAttribLocation("aTextureCoordinate")
    }

    fun getTextureMatrixLocation(): Int {
        return getUniformLocation("uTextureMatrix")
    }

    fun getTextureSamplerLocation(): Int {
        return getUniformLocation("uTextureSampler")
    }
}