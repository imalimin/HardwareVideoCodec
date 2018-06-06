/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * Gamma滤镜
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class GammaFilter(width: Int = 0,
                  height: Int = 0,
                  textureId: Int = -1) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mGammaLocation: Int = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mGammaLocation = getUniformLocation("gamma")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        setUniform1f(mGammaLocation, gamma)
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
        return "shader/fragment_gamma.sh"
    }

    private var gamma = 1f
    /**
     * 0 == index: gamma
     */
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                gamma = value / 100f * 3f
            }
        }
    }
}