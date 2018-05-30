/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/5/29.
 */
class BeautyFilter(width: Int = 0,
                   height: Int = 0,
                   textureId: Int = -1) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0

    private var paramsLocation = 0
    private var brightnessLocation = 0
    private var singleStepOffsetLocation = 0
    private var texelWidthLocation = 0
    private var texelHeightLocation = 0

    override fun init() {
        super.init()
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
        active()

        setParams(beautyLevel, toneLevel)
        setBrightLevel(brightLevel)
        setTexelOffset(texelWidthOffset)

        uniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_beauty.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_beauty.sh"
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
     * 0==index: beauty
     * 1==index: tone
     * 2==index: brightLevel
     * 3==index: texelOffset
     */
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                this.beautyLevel = value / 100f * 2.5f
            }
            1 -> {
                this.toneLevel = (value - 50) / 100f * 10
            }
            2 -> {
                this.brightLevel = value / 100f
            }
            3 -> {
                texelHeightOffset = (value - 50) / 100f * 2
                texelWidthOffset = texelHeightOffset
            }
        }
    }

    /**
     * -1 - 1
     */
    private fun setTexelOffset(texelOffset: Float) {
        setUniform1f(texelWidthLocation, texelOffset / 1440)
        setUniform1f(texelHeightLocation, texelOffset / 2100)
    }

    /**
     * 0 - 1
     */
    private fun setBrightLevel(brightLevel: Float) {
        setUniform1f(brightnessLocation, 0.6f * (-0.5f + brightLevel))
    }

    /**
     * beauty: 0 - 2.5, tone: -5 - 5
     */
    private fun setParams(beauty: Float, tone: Float) {
        val vector = FloatArray(4)
        vector[0] = 1.0f - 0.6f * beauty
        vector[1] = 1.0f - 0.3f * beauty
        vector[2] = 0.1f + 0.3f * tone
        vector[3] = 0.1f + 0.3f * tone
        setUniform4fv(paramsLocation, vector)
    }

    private fun setTexelSize(w: Float, h: Float) {
        setUniform2fv(singleStepOffsetLocation, floatArrayOf(2.0f / w, 2.0f / h))
    }
}