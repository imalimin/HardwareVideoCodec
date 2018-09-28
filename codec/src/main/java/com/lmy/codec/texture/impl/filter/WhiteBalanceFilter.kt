/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

/**
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class WhiteBalanceFilter(width: Int = 0,
                         height: Int = 0,
                         textureId: IntArray = IntArray(1),
                         private var mTemperature: Float = 0f,
                         private var mTint: Float = 0f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mTemperatureLocation = 0
    private var mTintLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mTemperatureLocation = getUniformLocation("temperature")
        mTintLocation = getUniformLocation("tint")
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(mTemperatureLocation, mTemperature)
        setUniform1f(mTintLocation, mTint)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_white_balance.glsl"
    }

    /**
     * 0 == index: Temperature
     * 1 == index: Tint
     */
    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_TEMPERATURE, (progress - 50) / 100f * 2,
                        IParams.PARAM_NONE
                ))
            }
            1 -> {
                setParams(floatArrayOf(
                        PARAM_TINT, progress / 100f,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_TEMPERATURE == cursor -> this.mTemperature = value
            PARAM_TINT == cursor -> this.mTint = value
        }
    }

    companion object {
        const val PARAM_TEMPERATURE = 100f
        const val PARAM_TINT = PARAM_TEMPERATURE + 1
    }
}