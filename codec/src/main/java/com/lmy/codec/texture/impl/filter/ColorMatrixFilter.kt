package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
open class ColorMatrixFilter(width: Int = 0,
                             height: Int = 0,
                             textureId: Int = -1,
                             private var mIntensity: Float = 0f,
                             private var mColorMatrix: FloatArray = MATRIX)
    : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mColorMatrixLocation: Int = 0
    private var mIntensityLocation: Int = 0


    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mColorMatrixLocation = getUniformLocation("colorMatrix")
        mIntensityLocation = getUniformLocation("intensity")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        setFloat(mIntensityLocation, mIntensity)
        setUniformMatrix4fv(mColorMatrixLocation, mColorMatrix)
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
        return "shader/fragment_color_matrix.sh"
    }

    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                mIntensity = value / 100f * 2
            }
        }
    }

    fun setColorMatrix(colorMatrix: FloatArray) {
        mColorMatrix = colorMatrix
    }

    companion object {
        val MATRIX: FloatArray = floatArrayOf(
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        )
    }
}