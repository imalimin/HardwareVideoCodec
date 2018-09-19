/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

class ChromaticFilter(width: Int = 0,
                      height: Int = 0,
                      textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var timeLocation = 0
    private var time = 0f
    private var startTime = 0L
    private var speed = 1f

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("sTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        timeLocation = getUniformLocation("time")
        startTime = System.currentTimeMillis()
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        time = (System.currentTimeMillis() - startTime) / 1000f * speed
        active(uTextureLocation)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        setUniform1f(timeLocation, time)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 ->
                speed = value / 100f * 10
        }
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_chromatic.glsl"
    }
}