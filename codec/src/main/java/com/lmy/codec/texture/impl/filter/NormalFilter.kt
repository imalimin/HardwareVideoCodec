/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import android.opengl.GLES20

/**
 * Created by lmyooyo@gmail.com on 2018/4/23.
 */
class NormalFilter(width: Int = 0,
                   height: Int = 0,
                   textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0

    init {
        name = "NormalFilter"
    }

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        GLES20.glUseProgram(GLES20.GL_NONE)
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_normal.glsl"
    }
}