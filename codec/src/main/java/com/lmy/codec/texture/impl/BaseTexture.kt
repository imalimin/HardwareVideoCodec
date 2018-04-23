package com.lmy.codec.texture.impl

import android.opengl.GLES20
import com.lmy.codec.texture.Texture
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
abstract class BaseTexture(var textureId: Int,
                           var buffer: FloatBuffer? = null,
                           var verticesBuffer: FloatBuffer? = null,
                           var shaderProgram: Int? = null,
                           var drawer: GLDrawer = GLDrawer()) : Texture {
    companion object {
        var COORDS_PER_VERTEX = 2
        var TEXTURE_COORDS_PER_VERTEX = 2
        private val DRAW_INDICES = shortArrayOf(0, 1, 2, 0, 2, 3)
        //每行前两个值为顶点坐标，后两个为纹理坐标
        val VERTICES_SQUARE = floatArrayOf(
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f)
    }

    init {
        buffer = createShapeVerticesBuffer(VERTICES_SQUARE)
    }

    fun createShapeVerticesBuffer(array: FloatArray): FloatBuffer {
        val result = ByteBuffer.allocateDirect(4 * array.size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        result.put(array).position(0)
        return result
    }

    fun createProgram(vertex: String, fragment: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment)
        return linkProgram(vertexShader!!, fragmentShader!!)
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

    fun getAttribLocation(name: String): Int {
        return GLES20.glGetAttribLocation(shaderProgram!!, name)
    }

    fun getUniformLocation(name: String): Int {
        return GLES20.glGetUniformLocation(shaderProgram!!, name)
    }

    fun release() {
        if (null != shaderProgram)
            GLES20.glDeleteProgram(shaderProgram!!)
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