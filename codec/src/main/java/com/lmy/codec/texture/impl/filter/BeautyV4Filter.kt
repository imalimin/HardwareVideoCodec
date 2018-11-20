package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

/**
 * Created by lmyooyo@gmail.com on 2018/9/5.
 */
class BeautyV4Filter(width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {
    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0
    private var texelWidthOffsetLocation = 0
    private var texelHeightOffsetLocation = 0
    private var paramsLocation = 0
    private var distanceLocation = 0
    private var brightnessLocation = 0

    private var texelWidthOffset: Float = 1f
    private var texelHeightOffset: Float = 1f
    private var params = 1f
    private var distance = 7f
    private var brightness = 0.015f

    override fun init() {
        super.init()
        texelWidthOffset = 1.6f / width
        texelHeightOffset = 1.6f / height
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        //美颜参数
        texelWidthOffsetLocation = getUniformLocation("texelWidthOffset")
        texelHeightOffsetLocation = getUniformLocation("texelHeightOffset")
        paramsLocation = getUniformLocation("params")
        distanceLocation = getUniformLocation("distanceNormalizationFactor")
        brightnessLocation = getUniformLocation("brightness")
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(texelWidthOffsetLocation, texelWidthOffset)
        setUniform1f(texelHeightOffsetLocation, texelHeightOffset)
        setUniform1f(paramsLocation, params)
        setUniform1f(distanceLocation, distance)
        setUniform1f(brightnessLocation, brightness)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_beauty_v4.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_beauty_v4.glsl"
    }

    override fun setValue(index: Int, progress: Int) {
        when (index) {
            0 -> {
                setParams(floatArrayOf(
                        PARAM_SMOOTH, progress / 100f * 2,
                        IParams.PARAM_NONE
                ))
            }
            1 -> {
                setParams(floatArrayOf(
                        PARAM_TEXEL_OFFSET, progress / 100f * 10,
                        IParams.PARAM_NONE
                ))
            }
            2 -> {
                setParams(floatArrayOf(
                        PARAM_BRIGHT, progress / 100f * 0.2f,
                        IParams.PARAM_NONE
                ))
            }
        }
    }

    override fun setParam(cursor: Float, value: Float) {
        when {
            PARAM_BRIGHT == cursor -> this.brightness = value
            PARAM_TEXEL_OFFSET == cursor -> this.distance = value
            PARAM_SMOOTH == cursor -> this.params = value
        }
    }

    companion object {
        const val PARAM_BRIGHT = 100f
        const val PARAM_TEXEL_OFFSET = PARAM_BRIGHT + 1
        const val PARAM_SMOOTH = PARAM_BRIGHT + 2
    }
}