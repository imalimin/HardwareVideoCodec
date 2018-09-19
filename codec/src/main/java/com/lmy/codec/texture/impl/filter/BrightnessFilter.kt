/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

/**
 * 亮度滤镜
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class BrightnessFilter(width: Int = 0,
                       height: Int = 0,
                       textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mBrightnessLocation = 0

    private var brightness = 0f

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mBrightnessLocation = getUniformLocation("brightness")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(mBrightnessLocation, brightness)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_brightness.glsl"
    }

    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                brightness = (value - 50) / 100f * 2
            }
        }
    }
}