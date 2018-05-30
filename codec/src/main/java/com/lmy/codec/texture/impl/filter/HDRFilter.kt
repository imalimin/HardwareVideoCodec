package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/4/28.
 */
class HDRFilter(width: Int, height: Int,
                textureId: Int = -1) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        uniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_hdr.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_hdr.sh"
    }
}