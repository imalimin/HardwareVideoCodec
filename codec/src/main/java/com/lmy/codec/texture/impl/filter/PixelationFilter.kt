/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * 像素化滤镜
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class PixelationFilter(width: Int = 0,
                       height: Int = 0,
                       textureId: Int = -1) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTexture = 0
    private var aTextureCoordinateLocation = 0

    private var imageWidthFactor = 0
    private var imageHeightFactor = 0
    private var pixel = 0

    override fun init() {
        super.init()
        valueWidthFactor = 1f / width
        valueHeightFactor = 1f / height
        aPositionLocation = getAttribLocation("aPosition")
        uTexture = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")

        imageWidthFactor = getUniformLocation("imageWidthFactor")
        imageHeightFactor = getUniformLocation("imageHeightFactor")
        pixel = getUniformLocation("pixel")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        setUniform1f(imageWidthFactor, valueWidthFactor)
        setUniform1f(imageHeightFactor, valueHeightFactor)
        setUniform1f(pixel, valuePixel)
        uniform1i(uTexture, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_pixelation.sh"
    }

    /**
     * 0 == index: pixel
     */
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                setValue(value.toFloat())
            }
        }
    }

    private var valueWidthFactor = 1f / width
    private var valueHeightFactor = 1f / height
    private var valuePixel = 1f
    /**
     * 0 == index: valuePixel
     */
    private fun setValue(value: Float) {
        this.valuePixel = if (value < 1)
            1f
        else
            value
    }
}