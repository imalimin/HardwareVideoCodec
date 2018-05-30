package com.lmy.codec.texture.impl.filter

/**
 * 色调滤镜
 * Created by lmyooyo@gmail.com on 2018/5/30.
 */
class HueFilter(width: Int = 0,
                height: Int = 0,
                textureId: Int = -1) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mHueLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")

        mHueLocation = getUniformLocation("hueAdjust")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        setFloat(mHueLocation, hueAdjust)
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
        return "shader/fragment_hue.sh"
    }

    private var hue = 0f
    private var hueAdjust = 0f
    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                hue = value.toFloat()
                hueAdjust = hue % 360.0f * Math.PI.toFloat() / 180.0f
            }
        }
    }
}