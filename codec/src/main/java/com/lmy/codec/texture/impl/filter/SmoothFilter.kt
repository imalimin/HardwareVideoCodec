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
class SmoothFilter(width: Int = 0,
                     height: Int = 0,
                   textureId: IntArray = IntArray(1),
                     private var mRadius: Int = 0) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mRadiusLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mRadiusLocation = getUniformLocation("radius")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        uniform1i(uTextureLocation, 0)
        setUniform1i(mRadiusLocation, mRadius)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_smooth.sh"
    }

    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                mRadius = (value / 100f * 10).toInt()
            }
        }
    }
}