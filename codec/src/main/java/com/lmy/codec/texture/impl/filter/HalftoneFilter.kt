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
class HalftoneFilter(width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mFractionalWidthOfPixelLocation: Int = 0
    private var mAspectRatioLocation: Int = 0
    private var mSize: Float = 0.03f


    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mFractionalWidthOfPixelLocation = getUniformLocation("fractionalWidthOfPixel")
        mAspectRatioLocation = getUniformLocation("aspectRatio")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(mFractionalWidthOfPixelLocation, mSize)
        setUniform1f(mAspectRatioLocation, height / width.toFloat())
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_halftone.glsl"
    }

    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_SIZE, progress / 100f * 0.1f,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_SIZE == cursor -> this.mSize = value
        }
    }

    companion object {
        const val PARAM_SIZE = 100f
    }
}