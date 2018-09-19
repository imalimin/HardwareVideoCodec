/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/6/6.
 */
class HalftoneFilter(width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1),
                     private var mFractionalWidthOfAPixel: Float = 0f,
                     private var mAspectRatio: Float = 0f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mFractionalWidthOfPixelLocation: Int = 0
    private var mAspectRatioLocation: Int = 0


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
        setUniform1f(mFractionalWidthOfPixelLocation, mFractionalWidthOfAPixel)
        setUniform1f(mAspectRatioLocation, mAspectRatio)
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

    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                mFractionalWidthOfAPixel = value / 100f * 0.1f
            }
            1 -> {
                mAspectRatio = value / 100f * 10
            }
        }
    }
}