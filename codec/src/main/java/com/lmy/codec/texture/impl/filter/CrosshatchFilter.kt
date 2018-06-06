package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/6/6.
 */
class CrosshatchFilter(width: Int = 0,
                       height: Int = 0,
                       textureId: Int = -1,
                       private var mCrossHatchSpacing: Float = 0f,
                       private var mLineWidth: Float = 0f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mCrossHatchSpacingLocation = 0
    private var mLineWidthLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mCrossHatchSpacingLocation = getUniformLocation("crossHatchSpacing")
        mLineWidthLocation = getUniformLocation("lineWidth")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        uniform1i(uTextureLocation, 0)
        setUniform1f(mCrossHatchSpacingLocation, mCrossHatchSpacing)
        setUniform1f(mLineWidthLocation, mLineWidth)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_crosshatch.sh"
    }

    /**
     * 0 == index: CrossHatchSpacing
     * 1 == index: LineWidth
     */
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                val crossHatchSpacing = value / 100f * 0.06f
                val singlePixelSpacing: Float = if (width != 0) {
                    1.0f / width
                } else {
                    1.0f / 2048.0f
                }
                mCrossHatchSpacing = if (crossHatchSpacing < singlePixelSpacing) {
                    singlePixelSpacing
                } else {
                    crossHatchSpacing
                }

            }
            1 -> {
                mLineWidth = value / 100f * 0.006f
            }
        }
    }
}