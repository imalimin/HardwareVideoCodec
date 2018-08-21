/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl

import android.opengl.GLES20
import com.lmy.codec.texture.Texture
import com.lmy.codec.util.debug_i
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
                           var drawer: GLDrawer = GLDrawer(),
                           var name: String = "BaseTexture") : Texture {
    companion object {
        var COORDS_PER_VERTEX = 2
        var TEXTURE_COORDS_PER_VERTEX = 2
    }

    private val bufferLock = Any()

    init {
        locationBuffer = createShapeVerticesBuffer(floatArrayOf(
                -1f, -1f,//LEFT,BOTTOM
                1f, -1f,//RIGHT,BOTTOM
                -1f, 1f,//LEFT,TOP
                1f, 1f//RIGHT,TOP
        ))
        textureBuffer = createShapeVerticesBuffer(floatArrayOf(
                0f, 0f,//LEFT,BOTTOM
                1f, 0f,//RIGHT,BOTTOM
                0f, 1f,//LEFT,TOP
                1f, 1f//RIGHT,TOP
        ))
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

    /**
     * 更新s,t,x,y
     * @param textureLocation 顶点纹理坐标
     * @param location        顶点位置
     */
    open fun updateLocation(textureLocation: FloatArray, location: FloatArray) {
        debug_i("location(${textureLocation[0]}, ${textureLocation[1]},\n" +
                "${textureLocation[2]}, ${textureLocation[3]},\n" +
                "${textureLocation[4]}, ${textureLocation[5]},\n" +
                "${textureLocation[6]}, ${textureLocation[7]})\n" +
                "(${location[0]}, ${location[1]},\n" +
                "${location[2]}, ${location[3]},\n" +
                "${location[4]}, ${location[5]},\n" +
                "${location[6]}, ${location[7]})")
        synchronized(bufferLock) {
            locationBuffer = createShapeVerticesBuffer(location)
            textureBuffer = createShapeVerticesBuffer(textureLocation)
        }
    }

    class GLDrawer {
        fun draw() {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }
    }
}