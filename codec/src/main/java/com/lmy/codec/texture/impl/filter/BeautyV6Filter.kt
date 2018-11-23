/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

/**
 * 双边滤波美颜
 */
class BeautyV6Filter(width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {
    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0
    private var texelWidthOffsetLocation = 0
    private var texelHeightOffsetLocation = 0
    private var paramsLocation = 0
    private var brightnessLocation = 0

    private var texelWidthOffset: Float = 1f
    private var texelHeightOffset: Float = 1f
    private val params = FloatArray(4)
    private var brightness = 0f
    private var texelOffset = 0f

    override fun init() {
        super.init()
        setParams(floatArrayOf(
                PARAM_BRIGHT, 0f,
                PARAM_BEAUTY, 0.66f,
                PARAM_SATURATE, 0f,
                PARAM_TEXEL_OFFSET, 1.5f,
                IParams.PARAM_NONE
        ))
        texelWidthOffset = 1f / width
        texelHeightOffset = 1f / height
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        //美颜参数
        texelWidthOffsetLocation = getUniformLocation("texelWidthOffset")
        texelHeightOffsetLocation = getUniformLocation("texelHeightOffset")
        paramsLocation = getUniformLocation("params")
        brightnessLocation = getUniformLocation("brightness")
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(texelWidthOffsetLocation, texelOffset * texelWidthOffset)
        setUniform1f(texelHeightOffsetLocation, texelOffset * texelHeightOffset)
        setUniform4fv(paramsLocation, params)
        setUniform1f(brightnessLocation, brightness)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_beauty_v4.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_beauty_v6.glsl"
    }

    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_BRIGHT, progress / 100f * 0.2f,
                        IParams.PARAM_NONE
                ))
            }
            1 -> {
                setParams(floatArrayOf(
                        PARAM_BEAUTY, progress / 100f * 2,
                        IParams.PARAM_NONE
                ))
            }
            2 -> {
                setParams(floatArrayOf(
                        PARAM_SATURATE, progress / 100f * 2,
                        IParams.PARAM_NONE
                ))
            }
            3 -> {
                setParams(floatArrayOf(
                        PARAM_TEXEL_OFFSET, progress / 100f * 10,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_BRIGHT == cursor -> this.brightness = value
            PARAM_BEAUTY == cursor -> {
                this.params[0] = 1.6f - 1.2f * value
                this.params[1] = 1.3f - 0.6f * value
            }
            PARAM_SATURATE == cursor -> {
                this.params[2] = -0.2f + 0.6f * value
                this.params[3] = -0.2f + 0.6f * value
            }
            PARAM_TEXEL_OFFSET == cursor -> {
                this.texelOffset = value
            }
        }
    }

    companion object {
        const val PARAM_BRIGHT = 100f
        const val PARAM_BEAUTY = PARAM_BRIGHT + 1//磨皮
        const val PARAM_SATURATE = PARAM_BRIGHT + 2//饱和度
        const val PARAM_TEXEL_OFFSET = PARAM_BRIGHT + 3//高斯模糊距离
    }
}