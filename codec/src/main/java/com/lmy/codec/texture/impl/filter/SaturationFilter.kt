/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

/**
 * 饱和度
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class SaturationFilter(width: Int = 0,
                       height: Int = 0,
                       textureId: IntArray = IntArray(1),
                       private var mSaturation: Float = 1f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mSaturationLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mSaturationLocation = getUniformLocation("saturation")
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(mSaturationLocation, mSaturation)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_saturation.glsl"
    }

    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_SATURATION, progress / 100f * 2,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_SATURATION == cursor -> this.mSaturation = value
        }
    }

    companion object {
        const val PARAM_SATURATION = 100f
    }
}