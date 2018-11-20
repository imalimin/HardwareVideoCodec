package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

class BlurDirectionFilter : BaseFilter() {
    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0
    private var texelWidthOffsetLocation = 0
    private var texelHeightOffsetLocation = 0
    private var directionLocation = 0

    private var texelWidthOffset: Float = 0f
    private var texelHeightOffset: Float = 0f
    private var blurSize: Float = 0f
    /**
     * direction <= 0:horizontal
     * direction > 0:vertical
     */
    private var direction: Int = 0

    override fun init() {
        super.init()
        texelWidthOffset = 1 / width.toFloat()
        texelHeightOffset = 1 / height.toFloat()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        texelWidthOffsetLocation = getUniformLocation("texelWidthOffset")
        texelHeightOffsetLocation = getUniformLocation("texelHeightOffset")
        directionLocation = getUniformLocation("direction")
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(texelWidthOffsetLocation, texelWidthOffset * blurSize)
        setUniform1f(texelHeightOffsetLocation, texelHeightOffset * blurSize)
        setUniform1i(directionLocation, direction)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_blur_direction.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_blur_direction.glsl"
    }

    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_DIRECTION, progress / 100f * 26,
                        IParams.PARAM_NONE
                ))
            }
            1 -> {
                setParams(floatArrayOf(
                        PARAM_BLUR_SIZE, progress / 100f * 2 - 1,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_BLUR_SIZE == cursor -> this.blurSize = value
            PARAM_DIRECTION == cursor -> this.direction = value.toInt()
        }
    }

    companion object {
        const val PARAM_BLUR_SIZE = 100f
        const val PARAM_DIRECTION = PARAM_BLUR_SIZE + 1
    }
}