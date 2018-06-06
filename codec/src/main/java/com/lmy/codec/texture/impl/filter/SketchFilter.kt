/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import com.lmy.codec.util.debug_v

/**
 * Created by lmyooyo@gmail.com on 2018/6/6.
 */
class SketchFilter(width: Int = 0,
                   height: Int = 0,
                   textureId: Int = -1,
                   private var mTexelWidth: Float = 0f,
                   private var mTexelHeight: Float = 0f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mUniformTexelWidthLocation: Int = 0
    private var mUniformTexelHeightLocation: Int = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mUniformTexelWidthLocation = getUniformLocation("texelWidth")
        mUniformTexelHeightLocation = getUniformLocation("texelHeight")
        setValue(0, 30)
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        uniform1i(uTextureLocation, 0)
        setUniform1f(mUniformTexelWidthLocation, mTexelWidth)
        setUniform1f(mUniformTexelHeightLocation, mTexelHeight)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_sketch.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_sketch.sh"
    }

    override fun setValue(index: Int, value: Int) {
        debug_v(index, value)
        when (index) {
            0 -> {
                val size = value / 100f * 10
                mTexelWidth = size / width
                mTexelHeight = size / height
            }
        }
    }
}