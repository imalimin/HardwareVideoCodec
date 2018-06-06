/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * 曝光度
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class ExposureFilter(width: Int = 0,
                     height: Int = 0,
                     textureId: Int = -1,
                     private var mExposure: Float = 0f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mExposureLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mExposureLocation = getUniformLocation("exposure")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        setUniform1f(mExposureLocation, mExposure)
        uniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_exposure.sh"
    }

    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                mExposure = (value - 50) / 100f * 10
            }
        }
    }
}