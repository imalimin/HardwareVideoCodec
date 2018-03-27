package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLES20.*
import com.lmy.codec.entity.Egl
import com.lmy.codec.util.debug_e
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
class ScreenTextureWrapper(override var surfaceTexture: SurfaceTexture? = null,
                           var egl: Egl? = null,
                           var buffer: FloatBuffer? = null,
                           var vertexShader: Int? = null,
                           var fragmentShader: Int? = null,
                           var mShaderProgram: Int? = null) : TextureWrapper() {
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
                //求此颜色的灰度值
                "  float fGrayColor = (0.3*vCameraColor.r + 0.59*vCameraColor.g + 0.11*vCameraColor.b);\n" +
                //将此灰度值作为输出颜色的RGB值，这样就会变成黑白滤镜
                "  gl_FragColor = vec4(fGrayColor, fGrayColor, fGrayColor, 1.0);\n" +
                "}\n"
        //每行前两个值为顶点坐标，后两个为纹理坐标
        private val VERTEX_DATA = floatArrayOf(1f, 1f, 1f, 1f, -1f, 1f, 0f, 1f, -1f, -1f, 0f, 0f, 1f, 1f, 1f, 1f, -1f, -1f, 0f, 0f, 1f, -1f, 1f, 0f)
    }

    init {
        if (null != surfaceTexture) {
            egl = Egl()
            egl!!.initEGL(surfaceTexture!!)
            egl!!.makeCurrent()
            initBuffer()
            vertexShader = loadShader(GL_VERTEX_SHADER, VERTEX_SHADER)
            fragmentShader = loadShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
            mShaderProgram = linkProgram(vertexShader!!, fragmentShader!!)
        } else {
            debug_e("Egl create failed")
        }
    }

    private fun initBuffer() {
        buffer = ByteBuffer.allocateDirect(VERTEX_DATA.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        buffer!!.put(VERTEX_DATA, 0, VERTEX_DATA.size).position(0)
    }

    /**
     * 加载着色器，GL_VERTEX_SHADER代表生成顶点着色器，GL_FRAGMENT_SHADER代表生成片段着色器
     */
    private fun loadShader(type: Int, shaderSource: String): Int {
        //创建Shader
        val shader = glCreateShader(type)
        if (shader == 0) {
            throw RuntimeException("Create Shader Failed!" + GLES20.glGetError())
        }
        //加载Shader代码
        glShaderSource(shader, shaderSource)
        //编译Shader
        glCompileShader(shader)
        return shader
    }

    /**
     * 将两个Shader链接至program中
     */
    private fun linkProgram(verShader: Int, fragShader: Int): Int {
        //创建program
        val program = glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Create Program Failed!" + GLES20.glGetError())
        }
        //附着顶点和片段着色器
        glAttachShader(program, verShader)
        glAttachShader(program, fragShader)
        //链接program
        glLinkProgram(program)
        //告诉OpenGL ES使用此program
        glUseProgram(program)
        return program
    }

    fun getPositionLocation(): Int {
        return glGetAttribLocation(mShaderProgram!!, "aPosition")
    }

    fun getTextureCoordinateLocation(): Int {
        return glGetAttribLocation(mShaderProgram!!, "aTextureCoordinate")
    }

    fun getTextureMatrixLocation(): Int {
        return getUniformLocation("uTextureMatrix")
    }

    fun getTextureSamplerLocation(): Int {
        return getUniformLocation("uTextureSampler")
    }

    private fun getUniformLocation(name: String): Int {
        return glGetUniformLocation(mShaderProgram!!, name)
    }
}