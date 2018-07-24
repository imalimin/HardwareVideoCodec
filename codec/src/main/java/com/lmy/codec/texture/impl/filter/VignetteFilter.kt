/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import android.graphics.PointF

/**
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class VignetteFilter(width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1),
                     private var mVignetteCenter: PointF = PointF(0.5f,0.5f),
                     private var mVignetteColor: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f),
                     private var mVignetteStart: Float = 0.5f,
                     private var mVignetteEnd: Float = 1f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mVignetteCenterLocation = 0
    private var mVignetteColorLocation = 0
    private var mVignetteStartLocation = 0
    private var mVignetteEndLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mVignetteCenterLocation = getUniformLocation("vignetteCenter")
        mVignetteColorLocation = getUniformLocation("vignetteColor")
        mVignetteStartLocation = getUniformLocation("vignetteStart")
        mVignetteEndLocation = getUniformLocation("vignetteEnd")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        setUniform2fv(mVignetteCenterLocation, floatArrayOf(mVignetteCenter.x, mVignetteCenter.y))
        setUniform3fv(mVignetteColorLocation, mVignetteColor)
        setUniform1f(mVignetteStartLocation, mVignetteStart)
        setUniform1f(mVignetteEndLocation, mVignetteEnd)
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
        return "shader/fragment_vignette.sh"
    }

    /**
     * 0 == index: VignetteCenter x
     * 1 == index: VignetteCenter y
     * 2 == index: VignetteStart
     * 3 == index: VignetteEnd
     */
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> mVignetteCenter.x = value / 100f
            1 -> mVignetteCenter.y = value / 100f
            2 -> mVignetteStart = value / 100f
            3 -> mVignetteEnd = value / 100f
        }
    }
}