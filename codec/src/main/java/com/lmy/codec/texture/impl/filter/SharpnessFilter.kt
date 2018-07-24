/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * 描边滤镜
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class SharpnessFilter(width: Int = 0,
                      height: Int = 0,
                      textureId: IntArray = IntArray(1),
                      private var mSharpness: Float = 0f,
                      private var mImageWidth: Float = 1f / width,
                      private var mImageHeight: Float = 1f / height) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mImageWidthFactorLocation = 0
    private var mImageHeightFactorLocation = 0
    private var mSharpnessLocation = 0

    override fun init() {
        super.init()
        mImageWidth = 1f / width
        mImageHeight = 1f / height
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mSharpnessLocation = getUniformLocation("sharpness")
        mImageWidthFactorLocation = getUniformLocation("imageWidthFactor")
        mImageHeightFactorLocation = getUniformLocation("imageHeightFactor")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        setUniform1f(mImageWidthFactorLocation, mImageWidth)
        setUniform1f(mImageHeightFactorLocation, mImageHeight)
        setUniform1f(mSharpnessLocation, mSharpness)
        uniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_sharpness.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_sharpness.sh"
    }

    /**
     * 0 == index: Sharpness
     */
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                mSharpness = (value - 50) / 100f * 8
            }
        }
    }
}