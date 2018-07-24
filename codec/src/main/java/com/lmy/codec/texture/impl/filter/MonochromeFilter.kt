/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * 单色
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class MonochromeFilter(width: Int = 0,
                       height: Int = 0,
                       textureId: IntArray = IntArray(1),
                       private var mIntensity: Float = 0.toFloat(),
                       private var mColor: FloatArray = floatArrayOf(0.6f, 0.45f, 0.3f, 1f)) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mIntensityLocation = 0
    private var mFilterColorLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mIntensityLocation = getUniformLocation("intensity")
        mFilterColorLocation = getUniformLocation("filterColor")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        setUniform1f(mIntensityLocation, mIntensity)
        setUniform3fv(mFilterColorLocation, mColor)
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
        return "shader/fragment_monochrome.sh"
    }

    /**
     * 0 == index: Intensity
     * 1 == index: R
     * 2 == index: G
     * 3 == index: B
     */
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                mIntensity = value / 100f
            }
            1 -> {
                mColor[0] = value / 100f
            }
            2 -> {
                mColor[1] = value / 100f
            }
            3 -> {
                mColor[2] = value / 100f
            }
        }
    }
}