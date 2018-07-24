/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl

import android.opengl.GLES20
import com.lmy.codec.texture.Texture
import com.lmy.codec.util.debug_e
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
abstract class BaseTexture(var textureId: IntArray,
                           var locationBuffer: FloatBuffer? = null,
                           var textureBuffer: FloatBuffer? = null,
                           var shaderProgram: Int? = null,
                           var drawer: GLDrawer = GLDrawer()) : Texture {
    companion object {
        var COORDS_PER_VERTEX = 2
        var TEXTURE_COORDS_PER_VERTEX = 2
    }

    private val bufferLock = Any()

    init {
        locationBuffer = createShapeVerticesBuffer(getLocationVertices(1f, 1f))
        textureBuffer = createShapeVerticesBuffer(getTextureLocationVertices(1f, 1f))
    }

    fun createShapeVerticesBuffer(array: FloatArray): FloatBuffer {
        val result = ByteBuffer.allocateDirect(4 * array.size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        result.put(array).position(0)
        return result
    }

    fun createProgram(vertex: String, fragment: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment)
        return linkProgram(vertexShader, fragmentShader)
    }

    /**
     * 加载着色器，GL_VERTEX_SHADER代表生成顶点着色器，GL_FRAGMENT_SHADER代表生成片段着色器
     */
    fun loadShader(type: Int, shaderSource: String): Int {
        //创建Shader
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) {
            throw RuntimeException("Create Shader Failed!" + GLES20.glGetError())
        }
        //加载Shader代码
        GLES20.glShaderSource(shader, shaderSource)
        //编译Shader
        GLES20.glCompileShader(shader)
        return shader
    }

    /**
     * 将两个Shader链接至program中
     */
    fun linkProgram(verShader: Int, fragShader: Int): Int {
        //创建program
        val program = GLES20.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Create Program Failed!" + GLES20.glGetError())
        }
        //附着顶点和片段着色器
        GLES20.glAttachShader(program, verShader)
        GLES20.glAttachShader(program, fragShader)
        //链接program
        GLES20.glLinkProgram(program)
        //告诉OpenGL ES使用此program
        GLES20.glUseProgram(program)
        return program
    }

    fun enableVertex(posLoc: Int, texLoc: Int) {
        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glEnableVertexAttribArray(texLoc)
        synchronized(bufferLock) {
            //xy
            GLES20.glVertexAttribPointer(posLoc, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * 4, locationBuffer)
            //st
            GLES20.glVertexAttribPointer(texLoc, TEXTURE_COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    TEXTURE_COORDS_PER_VERTEX * 4, textureBuffer)
        }
    }

    fun disableVertex(position: Int, coordinate: Int) {
        GLES20.glDisableVertexAttribArray(position)
        GLES20.glDisableVertexAttribArray(coordinate)
    }

    fun getAttribLocation(name: String): Int {
        return GLES20.glGetAttribLocation(shaderProgram!!, name)
    }

    fun getUniformLocation(name: String): Int {
        return GLES20.glGetUniformLocation(shaderProgram!!, name)
    }

    open fun release() {
        if (null != shaderProgram)
            GLES20.glDeleteProgram(shaderProgram!!)
    }

    open fun updateLocation(cropRatioWidth: Float, cropRatioHeight: Float) {
        synchronized(bufferLock) {
            locationBuffer = createShapeVerticesBuffer(getLocationVertices(cropRatioWidth, cropRatioHeight))
        }
    }

    fun updateTextureLocation(cropRatioWidth: Float, cropRatioHeight: Float) {
        synchronized(bufferLock) {
            textureBuffer = createShapeVerticesBuffer(getTextureLocationVertices(cropRatioWidth, cropRatioHeight))
        }
    }

    private fun getLocationVertices(cropRatioWidth: Float, cropRatioHeight: Float): FloatArray {
        val x = if (cropRatioWidth > 1) 1f else cropRatioWidth
        val y = if (cropRatioHeight > 1) 1f else cropRatioHeight
        val left = -x
        var right = -left
        val bottom = -y
        val top = -bottom
        debug_e("location($left, $top, $right, $bottom)")
        return floatArrayOf(
                left, bottom,//LEFT,BOTTOM
                right, bottom,//RIGHT,BOTTOM
                left, top,//LEFT,TOP
                right, top//RIGHT,TOP
        )
    }

    private fun getTextureLocationVertices(cropRatioWidth: Float, cropRatioHeight: Float): FloatArray {
        val x = if (cropRatioWidth > 1) 1f else cropRatioWidth
        val y = if (cropRatioHeight > 1) 1f else cropRatioHeight
        val left = (1 - x) / 2
        var right = left + x
        val bottom = (1 - y) / 2
        val top = bottom + y
        debug_e("crop($left, $top, $right, $bottom)")
        return floatArrayOf(
                left, bottom,//LEFT,BOTTOM
                right, bottom,//RIGHT,BOTTOM
                left, top,//LEFT,TOP
                right, top//RIGHT,TOP
        )
    }

    class GLDrawer {
        fun draw() {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }
    }
}