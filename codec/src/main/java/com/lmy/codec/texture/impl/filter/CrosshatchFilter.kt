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
class CrosshatchFilter(width: Int = 0,
                       height: Int = 0,
                       textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mCrossHatchSpacingLocation = 0
    private var mLineWidthLocation = 0
    private var mCrossHatchSpacing: Float = 0f
    private var mLineWidth: Float = 0f

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mCrossHatchSpacingLocation = getUniformLocation("crossHatchSpacing")
        mLineWidthLocation = getUniformLocation("lineWidth")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(mCrossHatchSpacingLocation, mCrossHatchSpacing)
        setUniform1f(mLineWidthLocation, mLineWidth)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_crosshatch.glsl"
    }

    /**
     * 0 == index: CrossHatchSpacing
     * 1 == index: LineWidth
     */
    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                val crossHatchSpacing = progress / 100f * 0.06f
                val singlePixelSpacing: Float = if (width != 0) {
                    1.0f / width
                } else {
                    1.0f / 2048.0f
                }
                val space = if (crossHatchSpacing < singlePixelSpacing) {
                    singlePixelSpacing
                } else {
                    crossHatchSpacing
                }
                setParams(floatArrayOf(
                        PARAM_SPACE, space,
                        IParams.PARAM_NONE
                ))
            }
            1 -> {
                setParams(floatArrayOf(
                        PARAM_STROKE, progress / 100f * 0.006f,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_STROKE == cursor -> this.mLineWidth = value
            PARAM_SPACE == cursor -> this.mCrossHatchSpacing = value
        }
    }

    companion object {
        const val PARAM_STROKE = 100f
        const val PARAM_SPACE = PARAM_STROKE + 1
    }
}