/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

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

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(mIntensityLocation, mIntensity)
        setUniform3fv(mFilterColorLocation, mColor)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_monochrome.glsl"
    }

    /**
     * 0 == index: Intensity
     * 1 == index: R
     * 2 == index: G
     * 3 == index: B
     */
    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_IDTENSITY, progress / 100f,
                        IParams.PARAM_NONE
                ))
            }
            1 -> {
                setParams(floatArrayOf(
                        PARAM_R, progress / 100f,
                        IParams.PARAM_NONE
                ))
            }
            2 -> {
                setParams(floatArrayOf(
                        PARAM_G, progress / 100f,
                        IParams.PARAM_NONE
                ))
            }
            3 -> {
                setParams(floatArrayOf(
                        PARAM_B, progress / 100f,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_IDTENSITY == cursor -> this.mIntensity = value
            PARAM_R == cursor -> this.mColor[0] = value
            PARAM_G == cursor -> this.mColor[1] = value
            PARAM_B == cursor -> this.mColor[2] = value
        }
    }

    companion object {
        const val PARAM_IDTENSITY = 100f
        const val PARAM_R = PARAM_IDTENSITY + 1
        const val PARAM_G = PARAM_IDTENSITY + 2
        const val PARAM_B = PARAM_IDTENSITY + 4
    }
}