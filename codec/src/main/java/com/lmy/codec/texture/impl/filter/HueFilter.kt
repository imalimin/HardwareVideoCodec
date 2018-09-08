/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

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

    override fun drawTexture(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(mHueLocation, hueAdjust)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_hue.sh"
    }

    private var hue = 0f
    private var hueAdjust = 0f
    /**
     * 0 == index: hue
     */
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                hue = value.toFloat()
                hueAdjust = hue % 360.0f * Math.PI.toFloat() / 180.0f
            }
        }
    }
}