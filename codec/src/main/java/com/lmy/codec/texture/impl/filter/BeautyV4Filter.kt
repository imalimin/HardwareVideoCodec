package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/9/5.
 */
class BeautyV4Filter(width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {
    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0
    private var paramsLocation = 0
    private var distanceLocation = 0
    private var brightnessLocation = 0

    private var params = 1f
    private var distance = 7f
    private var brightness = 0.015f

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        //美颜参数
        paramsLocation = getUniformLocation("params")
        distanceLocation = getUniformLocation("distanceNormalizationFactor")
        brightnessLocation = getUniformLocation("brightness")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        setUniform1f(paramsLocation, params)
        setUniform1f(distanceLocation, distance)
        setUniform1f(brightnessLocation, brightness)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_beauty_v4.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_beauty_v4.sh"
    }

    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                this.params = value / 100f * 2
            }
            1 -> {
                this.distance = value / 100f * 10
            }
            2 -> {
                this.brightness = value / 100f * 0.2f
            }
        }
    }
}