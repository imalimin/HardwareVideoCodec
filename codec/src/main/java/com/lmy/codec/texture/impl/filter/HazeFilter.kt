/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

/**
 * Created by lmyooyo@gmail.com on 2018/6/6.
 */
class HazeFilter(width: Int = 0,
                 height: Int = 0,
                 textureId: IntArray = IntArray(1),
                 private var mDistance: Float = 0f,
                 private var mSlope: Float = 0f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mDistanceLocation: Int = 0
    private var mSlopeLocation: Int = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mDistanceLocation = getUniformLocation("distance")
        mSlopeLocation = getUniformLocation("slope")
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(mDistanceLocation, mDistance)
        setUniform1f(mSlopeLocation, mSlope)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_haze.glsl"
    }

    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_DISTANCE, (progress - 50) / 100f * 0.6f,
                        IParams.PARAM_NONE
                ))
            }
            1 -> {
                setParams(floatArrayOf(
                        PARAM_SLOPE, (progress - 50) / 100f * 0.6f,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_DISTANCE == cursor -> this.mDistance = value
            PARAM_SLOPE == cursor -> this.mSlope = value
        }
    }

    companion object {
        const val PARAM_DISTANCE = 100f
        const val PARAM_SLOPE = PARAM_DISTANCE + 1
    }
}