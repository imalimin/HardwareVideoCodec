/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl

import android.opengl.GLES20
import android.opengl.GLES30
import com.lmy.codec.texture.Texture
import com.lmy.codec.util.debug_i
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
abstract class BaseTexture(var textureId: IntArray,
                           var name: String = "BaseTexture") : Texture {
    companion object {
        var COORDS_PER_VERTEX = 2
        var TEXTURE_COORDS_PER_VERTEX = 2
        val COORDS_BYTE_SIZE = COORDS_PER_VERTEX * 4 * 4
    }

    private val lock = Any()
    private var enableVAO = false
    var shaderProgram: Int? = null
    var drawer: GLDrawer = GLDrawer()
    private var position: FloatBuffer? = null
    private var texCoordinate: FloatBuffer? = null
    private var requestUpdateLocation: Boolean = false
    private var vbos: IntArray = IntArray(1)
    private var vao: IntArray? = null

    init {
        createVBOs()
        updateLocation(floatArrayOf(
                0f, 0f,//LEFT,BOTTOM
                1f, 0f,//RIGHT,BOTTOM
                0f, 1f,//LEFT,TOP
                1f, 1f//RIGHT,TOP
        ), floatArrayOf(
                -1f, -1f,//LEFT,BOTTOM
                1f, -1f,//RIGHT,BOTTOM
                -1f, 1f,//LEFT,TOP
                1f, 1f//RIGHT,TOP
        ))
    }

    private fun createVBOs() {
        GLES20.glGenBuffers(vbos.size, vbos, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, COORDS_BYTE_SIZE * 2
                , null, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, GLES20.GL_NONE)
    }

    private fun updateVBOs() {
        synchronized(lock) {
            if (!requestUpdateLocation) return
            requestUpdateLocation = false
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0])
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, COORDS_BYTE_SIZE, position!!)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, COORDS_BYTE_SIZE, COORDS_BYTE_SIZE, texCoordinate!!)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, GLES20.GL_NONE)
    }

    private fun createShapeVerticesBuffer(array: FloatArray): FloatBuffer {
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

    private fun enableVAO(posLoc: Int, texLoc: Int) {
        if (null == vao) {
            vao = IntArray(1)
            GLES30.glGenVertexArrays(vao!!.size, vao!!, 0)
            GLES30.glBindVertexArray(vao!![0])
            GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0])

            GLES30.glEnableVertexAttribArray(posLoc)
            GLES30.glEnableVertexAttribArray(texLoc)

            GLES30.glVertexAttribPointer(posLoc, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * 4, 0)
            GLES30.glVertexAttribPointer(texLoc, TEXTURE_COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    TEXTURE_COORDS_PER_VERTEX * 4, COORDS_BYTE_SIZE)
            GLES30.glBindVertexArray(GLES30.GL_NONE)
        }
        GLES30.glBindVertexArray(vao!![0])
    }

    fun enableVertex(posLoc: Int, texLoc: Int) {
        updateVBOs()
        if (enableVAO) {
            enableVAO(posLoc, texLoc)
            return
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0])
        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glEnableVertexAttribArray(texLoc)
        //xy
        GLES20.glVertexAttribPointer(posLoc, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                COORDS_PER_VERTEX * 4, 0)
        //st
        GLES20.glVertexAttribPointer(texLoc, TEXTURE_COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                TEXTURE_COORDS_PER_VERTEX * 4, COORDS_BYTE_SIZE)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, GLES20.GL_NONE)
    }

    fun disableVertex(position: Int, coordinate: Int) {
        if (enableVAO) {
            GLES30.glBindVertexArray(GLES30.GL_NONE)
            return
        }
        GLES20.glDisableVertexAttribArray(position)
        GLES20.glDisableVertexAttribArray(coordinate)
    }

    fun getAttribLocation(name: String): Int {
        return GLES20.glGetAttribLocation(shaderProgram!!, name)
    }

    fun getUniformLocation(name: String): Int {
        return GLES20.glGetUniformLocation(shaderProgram!!, name)
    }

    fun setUniform1i(location: Int, value: Int) {
        GLES20.glUniform1i(location, value)
    }

    fun setUniform1f(location: Int, floatValue: Float) {
        GLES20.glUniform1f(location, floatValue)
    }

    fun setUniform2fv(location: Int, arrayValue: FloatArray) {
        GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    fun setUniform3fv(location: Int, arrayValue: FloatArray) {
        GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    fun setUniform4fv(location: Int, arrayValue: FloatArray) {
        GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    fun setUniformMatrix4fv(location: Int, arrayValue: FloatArray) {
        GLES20.glUniformMatrix4fv(location, 1, false, FloatBuffer.wrap(arrayValue))
    }

    open fun release() {
        GLES20.glDeleteBuffers(vbos.size, vbos, 0)
        if (null != vao) {
            GLES30.glDeleteVertexArrays(vao!!.size, vao!!, 0)
        }
        if (null != shaderProgram)
            GLES20.glDeleteProgram(shaderProgram!!)
    }

    /**
     * 更新s,t,x,y
     * @param texCoordinate 纹理坐标
     * @param position  顶点位置
     */
    fun updateLocation(texCoordinate: FloatArray, position: FloatArray) {
        debug_i("location(${texCoordinate[0]}, ${texCoordinate[1]},\n" +
                "${texCoordinate[2]}, ${texCoordinate[3]},\n" +
                "${texCoordinate[4]}, ${texCoordinate[5]},\n" +
                "${texCoordinate[6]}, ${texCoordinate[7]})\n" +
                "(${position[0]}, ${position[1]},\n" +
                "${position[2]}, ${position[3]},\n" +
                "${position[4]}, ${position[5]},\n" +
                "${position[6]}, ${position[7]})")
        this.position = createShapeVerticesBuffer(position)
        this.texCoordinate = createShapeVerticesBuffer(texCoordinate)
        synchronized(lock) {
            this.requestUpdateLocation = true
        }
    }

    class GLDrawer {
        fun draw() {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }
    }
}