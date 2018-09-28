/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import android.graphics.PointF
import com.lmy.codec.texture.IParams

/**
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class VignetteFilter(width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1),
                     private var mVignetteCenter: PointF = PointF(0.5f, 0.5f),
                     private var mVignetteColor: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f),
                     private var mVignetteStart: Float = 0.5f,
                     private var mVignetteEnd: Float = 1f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mVignetteCenterLocation = 0
    private var mVignetteColorLocation = 0
    private var mVignetteStartLocation = 0
    private var mVignetteEndLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mVignetteCenterLocation = getUniformLocation("vignetteCenter")
        mVignetteColorLocation = getUniformLocation("vignetteColor")
        mVignetteStartLocation = getUniformLocation("vignetteStart")
        mVignetteEndLocation = getUniformLocation("vignetteEnd")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform2fv(mVignetteCenterLocation, floatArrayOf(mVignetteCenter.x, mVignetteCenter.y))
        setUniform3fv(mVignetteColorLocation, mVignetteColor)
        setUniform1f(mVignetteStartLocation, mVignetteStart)
        setUniform1f(mVignetteEndLocation, mVignetteEnd)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_vignette.glsl"
    }

    /**
     * 0 == index: VignetteCenter x
     * 1 == index: VignetteCenter y
     * 2 == index: VignetteStart
     * 3 == index: VignetteEnd
     */
    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_CENTER_X, progress / 100f,
                        IParams.PARAM_NONE
                ))
            }
            1 -> {
                setParams(floatArrayOf(
                        PARAM_CENTER_Y, progress / 100f,
                        IParams.PARAM_NONE
                ))
            }
            2 -> {
                setParams(floatArrayOf(
                        PARAM_START, progress / 100f,
                        IParams.PARAM_NONE
                ))
            }
            3 -> {
                setParams(floatArrayOf(
                        PARAM_END, progress / 100f,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_CENTER_X == cursor -> this.mVignetteCenter.x = value
            PARAM_CENTER_Y == cursor -> this.mVignetteCenter.y = value
            PARAM_START == cursor -> this.mVignetteStart = value
            PARAM_END == cursor -> this.mVignetteEnd = value
        }
    }

    companion object {
        const val PARAM_CENTER_X = 100f
        const val PARAM_CENTER_Y = PARAM_CENTER_X + 1
        const val PARAM_START = PARAM_CENTER_X + 2
        const val PARAM_END = PARAM_CENTER_X + 3
    }
}