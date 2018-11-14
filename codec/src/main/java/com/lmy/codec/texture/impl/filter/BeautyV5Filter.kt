package com.lmy.codec.texture.impl.filter

class BeautyV5Filter(width: Int = 0,
                     height: Int = 0,
                     textureId: IntArray = IntArray(1)) : BaseFilter(width, height, textureId) {
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

    override fun getVertex(): String {
        return "shader/vertex_beauty_v5.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_beauty_v5.glsl"
    }
}