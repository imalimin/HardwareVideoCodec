package com.lmy.codec.texture.impl

import android.opengl.GLES20
import java.nio.FloatBuffer

/**
 * 美颜滤镜
 * Created by lmyooyo@gmail.com on 2018/3/30.
 */
class BeautyTexture(var inputTextureId: Int,
                    var drawer: BaseFrameBufferTexture.GLDrawer) : BaseTexture() {

    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0

    private var paramsLocation = 0
    private var brightnessLocation = 0
    private var singleStepOffsetLocation = 0
    private var texelWidthLocation = 0
    private var texelHeightLocation = 0

    init {
        verticesBuffer = createShapeVerticesBuffer(VERTICES_SCREEN)
        createProgram()
    }

    private fun createProgram() {
        shaderProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        //美颜参数
        paramsLocation = getUniformLocation("params")
        brightnessLocation = getUniformLocation("brightness")
        singleStepOffsetLocation = getUniformLocation("singleStepOffset")
        texelWidthLocation = getUniformLocation("texelWidthOffset")
        texelHeightLocation = getUniformLocation("texelHeightOffset")

    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        GLES20.glUseProgram(shaderProgram!!)

        setParams(beautyLevel, toneLevel)
        setBrightLevel(brightLevel)
        setTexelOffset(texelWidthOffset)

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
    }

    private var texelHeightOffset = 0f
    private var texelWidthOffset = 0f
    private var toneLevel = 0f
    private var beautyLevel = 0f
    private var brightLevel = 0f

    fun setTexelOffset(texelOffset: Float) {
        texelHeightOffset = texelOffset
        texelWidthOffset = texelHeightOffset
        setFloat(texelWidthLocation, texelOffset / 1440)
        setFloat(texelHeightLocation, texelOffset / 2100)
    }

    private fun setToneLevel(toneLeve: Float) {
        this.toneLevel = toneLeve
        setParams(beautyLevel, toneLevel)
    }

    private fun setBeautyLevel(beautyLeve: Float) {
        this.beautyLevel = beautyLeve
        setParams(beautyLevel, toneLevel)
    }

    fun setBrightLevel(brightLevel: Float) {
        this.brightLevel = brightLevel
        setFloat(brightnessLocation, 0.6f * (-0.5f + brightLevel))
    }

    fun setParams(beauty: Float, tone: Float) {
        this.beautyLevel = beauty
        this.toneLevel = tone
        val vector = FloatArray(4)
        vector[0] = 1.0f - 0.6f * beauty
        vector[1] = 1.0f - 0.3f * beauty
        vector[2] = 0.1f + 0.3f * tone
        vector[3] = 0.1f + 0.3f * tone
        setFloatVec4(paramsLocation, vector)
    }

    private fun setTexelSize(w: Float, h: Float) {
        setFloatVec2(singleStepOffsetLocation, floatArrayOf(2.0f / w, 2.0f / h))
    }


    private fun setFloat(location: Int, floatValue: Float) {
        GLES20.glUniform1f(location, floatValue)
    }

    private fun setFloatVec2(location: Int, arrayValue: FloatArray) {
        GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    private fun setFloatVec3(location: Int, arrayValue: FloatArray) {
        GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    private fun setFloatVec4(location: Int, arrayValue: FloatArray) {
        GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    companion object {
        private val VERTICES_SCREEN = floatArrayOf(
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f)
        private val VERTEX_SHADER = "" +
                "attribute vec4 aPosition;\n" +
                "attribute vec2 aTextureCoord;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main(){\n" +
                "    gl_Position= aPosition;\n" +
                "    vTextureCoord = aTextureCoord;\n" +
                "}"
        private val FRAGMENT_SHADER = "" +
                "precision highp float;\n" +
                "   varying highp vec2 vTextureCoord;\n" +
                "\n" +
                "    uniform sampler2D uTexture;\n" +
                "\n" +
                "    uniform highp vec2 singleStepOffset;\n" +
                "    uniform highp vec4 params;\n" +
                "    uniform highp float brightness;\n" +
                "    uniform float texelWidthOffset;\n" +
                "    uniform float texelHeightOffset;\n" +
                "\n" +
                "    const highp vec3 W = vec3(0.299, 0.587, 0.114);\n" +
                "    const highp mat3 saturateMatrix = mat3(\n" +
                "        1.1102, -0.0598, -0.061,\n" +
                "        -0.0774, 1.0826, -0.1186,\n" +
                "        -0.0228, -0.0228, 1.1772);\n" +
                "    highp vec2 blurCoordinates[24];\n" +
                "\n" +
                "    highp float hardLight(highp float color) {\n" +
                "    if (color <= 0.5)\n" +
                "        color = color * color * 2.0;\n" +
                "    else\n" +
                "        color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);\n" +
                "    return color;\n" +
                "}\n" +
                "\n" +
                "    void main(){\n" +
                "    highp vec3 centralColor = texture2D(uTexture, vTextureCoord).rgb;\n" +
                "    vec2 singleStepOffset=vec2(texelWidthOffset,texelHeightOffset);\n" +
                "    blurCoordinates[0] = vTextureCoord.xy + singleStepOffset * vec2(0.0, -10.0);\n" +
                "    blurCoordinates[1] = vTextureCoord.xy + singleStepOffset * vec2(0.0, 10.0);\n" +
                "    blurCoordinates[2] = vTextureCoord.xy + singleStepOffset * vec2(-10.0, 0.0);\n" +
                "    blurCoordinates[3] = vTextureCoord.xy + singleStepOffset * vec2(10.0, 0.0);\n" +
                "    blurCoordinates[4] = vTextureCoord.xy + singleStepOffset * vec2(5.0, -8.0);\n" +
                "    blurCoordinates[5] = vTextureCoord.xy + singleStepOffset * vec2(5.0, 8.0);\n" +
                "    blurCoordinates[6] = vTextureCoord.xy + singleStepOffset * vec2(-5.0, 8.0);\n" +
                "    blurCoordinates[7] = vTextureCoord.xy + singleStepOffset * vec2(-5.0, -8.0);\n" +
                "    blurCoordinates[8] = vTextureCoord.xy + singleStepOffset * vec2(8.0, -5.0);\n" +
                "    blurCoordinates[9] = vTextureCoord.xy + singleStepOffset * vec2(8.0, 5.0);\n" +
                "    blurCoordinates[10] = vTextureCoord.xy + singleStepOffset * vec2(-8.0, 5.0);\n" +
                "    blurCoordinates[11] = vTextureCoord.xy + singleStepOffset * vec2(-8.0, -5.0);\n" +
                "    blurCoordinates[12] = vTextureCoord.xy + singleStepOffset * vec2(0.0, -6.0);\n" +
                "    blurCoordinates[13] = vTextureCoord.xy + singleStepOffset * vec2(0.0, 6.0);\n" +
                "    blurCoordinates[14] = vTextureCoord.xy + singleStepOffset * vec2(6.0, 0.0);\n" +
                "    blurCoordinates[15] = vTextureCoord.xy + singleStepOffset * vec2(-6.0, 0.0);\n" +
                "    blurCoordinates[16] = vTextureCoord.xy + singleStepOffset * vec2(-4.0, -4.0);\n" +
                "    blurCoordinates[17] = vTextureCoord.xy + singleStepOffset * vec2(-4.0, 4.0);\n" +
                "    blurCoordinates[18] = vTextureCoord.xy + singleStepOffset * vec2(4.0, -4.0);\n" +
                "    blurCoordinates[19] = vTextureCoord.xy + singleStepOffset * vec2(4.0, 4.0);\n" +
                "    blurCoordinates[20] = vTextureCoord.xy + singleStepOffset * vec2(-2.0, -2.0);\n" +
                "    blurCoordinates[21] = vTextureCoord.xy + singleStepOffset * vec2(-2.0, 2.0);\n" +
                "    blurCoordinates[22] = vTextureCoord.xy + singleStepOffset * vec2(2.0, -2.0);\n" +
                "    blurCoordinates[23] = vTextureCoord.xy + singleStepOffset * vec2(2.0, 2.0);\n" +
                "\n" +
                "    highp float sampleColor = centralColor.g * 22.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[0]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[1]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[2]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[3]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[4]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[5]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[6]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[7]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[8]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[9]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[10]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[11]).g;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[12]).g * 2.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[13]).g * 2.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[14]).g * 2.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[15]).g * 2.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[16]).g * 2.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[17]).g * 2.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[18]).g * 2.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[19]).g * 2.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[20]).g * 3.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[21]).g * 3.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[22]).g * 3.0;\n" +
                "    sampleColor += texture2D(uTexture, blurCoordinates[23]).g * 3.0;\n" +
                "\n" +
                "    sampleColor = sampleColor / 62.0;\n" +
                "\n" +
                "    highp float highPass = centralColor.g - sampleColor + 0.5;\n" +
                "\n" +
                "    for (int i = 0; i < 5; i++) {\n" +
                "        highPass = hardLight(highPass);\n" +
                "    }\n" +
                "    highp float lumance = dot(centralColor, W);\n" +
                "\n" +
                "    highp float alpha = pow(lumance, params.r);\n" +
                "\n" +
                "    highp vec3 smoothColor = centralColor + (centralColor-vec3(highPass))*alpha*0.1;\n" +
                "\n" +
                "    smoothColor.r = clamp(pow(smoothColor.r, params.g), 0.0, 1.0);\n" +
                "    smoothColor.g = clamp(pow(smoothColor.g, params.g), 0.0, 1.0);\n" +
                "    smoothColor.b = clamp(pow(smoothColor.b, params.g), 0.0, 1.0);\n" +
                "\n" +
                "    highp vec3 lvse = vec3(1.0)-(vec3(1.0)-smoothColor)*(vec3(1.0)-centralColor);\n" +
                "    highp vec3 bianliang = max(smoothColor, centralColor);\n" +
                "    highp vec3 rouguang = 2.0*centralColor*smoothColor + centralColor*centralColor - 2.0*centralColor*centralColor*smoothColor;\n" +
                "\n" +
                "    gl_FragColor = vec4(mix(centralColor, lvse, alpha), 1.0);\n" +
                "    gl_FragColor.rgb = mix(gl_FragColor.rgb, bianliang, alpha);\n" +
                "    gl_FragColor.rgb = mix(gl_FragColor.rgb, rouguang, params.b);\n" +
                "\n" +
                "    highp vec3 satcolor = gl_FragColor.rgb * saturateMatrix;\n" +
                "    gl_FragColor.rgb = mix(gl_FragColor.rgb, satcolor, params.a);\n" +
                "    gl_FragColor.rgb = vec3(gl_FragColor.rgb + vec3(brightness));\n" +
                "}"
    }
}