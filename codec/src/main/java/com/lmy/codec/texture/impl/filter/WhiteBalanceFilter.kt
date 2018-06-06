/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class WhiteBalanceFilter(width: Int = 0,
                         height: Int = 0,
                         textureId: Int = -1,
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

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        setUniform1f(mTemperatureLocation, mTemperature)
        setUniform1f(mTintLocation, mTint)
        uniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_white_balance.sh"
    }

    /**
     * 0 == index: Temperature
     * 1 == index: Tint
     */
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                mTemperature = (value - 50) / 100f * 2
            }
            1 -> {
                mTint = value / 100f
            }
        }
    }
}