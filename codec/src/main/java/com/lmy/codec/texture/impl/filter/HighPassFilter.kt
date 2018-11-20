package com.lmy.codec.texture.impl.filter

class HighPassFilter(var srcTextureId: IntArray,
                     width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1)) : BaseMultipleSamplerFilter(width, height, textureId) {
    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uTextureLocation = 0
    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
    }

    override fun draw(transformMatrix: FloatArray?) {
        active(uTextureLocation)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getSamplers(): Array<Sampler>? {
        return arrayOf(
                Texture(srcTextureId, "sTexture2")
        )
    }

    override fun getVertex(): String {
        return "shader/vertex_high_pass.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_high_pass.glsl"
    }
}