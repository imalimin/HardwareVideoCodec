package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

class BeautyV5Filter(width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {
    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0
    private var texelWidthOffsetLocation = 0
    private var texelHeightOffsetLocation = 0

    private var texelWidthOffset: Float = 0f
    private var texelHeightOffset: Float = 0f
    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        texelWidthOffsetLocation = getUniformLocation("texelWidthOffset")
        texelHeightOffsetLocation = getUniformLocation("texelHeightOffset")
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(texelWidthOffsetLocation, 1f / width)
        setUniform1f(texelHeightOffsetLocation, 1f / height)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_beauty_v5.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_beauty_v5.glsl"
    }

    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_WIDTH_OFFSET, progress / 100f * 26 / width,
                        IParams.PARAM_NONE
                ))
            }
            1 -> {
                setParams(floatArrayOf(
                        PARAM_HEIGHT_OFFSET, progress / 100f * 26 / height,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_WIDTH_OFFSET == cursor -> this.texelWidthOffset = value
            PARAM_HEIGHT_OFFSET == cursor -> this.texelHeightOffset = value
        }
    }

    companion object {
        const val PARAM_WIDTH_OFFSET = 100f
        const val PARAM_HEIGHT_OFFSET = PARAM_WIDTH_OFFSET + 1
    }
}