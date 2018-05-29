/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import android.opengl.GLES20
import com.lmy.codec.BaseApplication
import com.lmy.codec.helper.AssetsHelper
import com.lmy.codec.texture.impl.BaseTextureFilter
import java.nio.FloatBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/5/29.
 */
class BeautyTextureFilter(width: Int = 0,
                          height: Int = 0,
                          textureId: Int = -1) : BaseTextureFilter(width, height, textureId) {

    companion object {
        private val VERTICES_SCREEN = floatArrayOf(
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f)
    }

    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0

    private var paramsLocation = 0
    private var brightnessLocation = 0
    private var singleStepOffsetLocation = 0
    private var texelWidthLocation = 0
    private var texelHeightLocation = 0

    override fun init() {
        verticesBuffer = createShapeVerticesBuffer(VERTICES_SCREEN)
        createProgram()
        initFrameBuffer()
    }

    private fun createProgram() {
        shaderProgram = createProgram(AssetsHelper.read(BaseApplication.assetManager(), "shader/vertex_beauty.sh"),
                AssetsHelper.read(BaseApplication.assetManager(), "shader/fragment_beauty.sh"))
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
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!!)
        GLES20.glUseProgram(shaderProgram!!)

        setParams(beautyLevel, toneLevel)
        setBrightLevel(brightLevel)
        setTexelOffset(texelWidthOffset)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation, buffer!!, verticesBuffer!!)

        drawer.draw()

        GLES20.glFinish()
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
    }

    private var texelHeightOffset = 0f
    private var texelWidthOffset = 0f
    private var toneLevel = 0f
    private var beautyLevel = 0f
    private var brightLevel = 0f

    private fun setToneLevel(toneLeve: Float) {
        this.toneLevel = toneLeve
        setParams(beautyLevel, toneLevel)
    }

    private fun setBeautyLevel(beautyLeve: Float) {
        this.beautyLevel = beautyLeve
        setParams(beautyLevel, toneLevel)
    }

    /**
     * -1 - 1
     */
    fun setTexelOffset(texelOffset: Float) {
        texelHeightOffset = texelOffset
        texelWidthOffset = texelHeightOffset
        setFloat(texelWidthLocation, texelOffset / 1440)
        setFloat(texelHeightLocation, texelOffset / 2100)
    }

    /**
     * 0 - 1
     */
    fun setBrightLevel(brightLevel: Float) {
        this.brightLevel = brightLevel
        setFloat(brightnessLocation, 0.6f * (-0.5f + brightLevel))
    }

    /**
     * beauty: 0 - 2.5, tone: -5 - 5
     */
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
}