/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

/**
 * 色调滤镜
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class HueFilter(width: Int = 0,
                height: Int = 0,
                textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mHueLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")

        mHueLocation = getUniformLocation("hueAdjust")
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(mHueLocation, hue)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_hue.glsl"
    }

    private var hue = 0f
    /**
     * 0 == index: hue
     */
    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_HUE, progress % 360.0f * Math.PI.toFloat() / 180.0f,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_HUE == cursor -> this.hue = value
        }
    }

    companion object {
        const val PARAM_HUE = 100f
    }
}