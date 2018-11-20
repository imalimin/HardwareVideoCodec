package com.lmy.codec.texture.impl.filter

import com.lmy.codec.texture.IParams

class BeautyV5Filter private constructor(filter: BaseFilter) : GroupFilter(filter, false) {
    private val normalFilter = filter
    private val horizontalBlurFilter = BlurDirectionFilter()
            .apply {
                setParams(floatArrayOf(
                        BlurDirectionFilter.PARAM_DIRECTION, 0f,
                        BlurDirectionFilter.PARAM_BLUR_SIZE, 1f,
                        IParams.PARAM_NONE
                ))
            }
    private val verticalBlurFilter = BlurDirectionFilter()
            .apply {
                setParams(floatArrayOf(
                        BlurDirectionFilter.PARAM_DIRECTION, 1f,
                        BlurDirectionFilter.PARAM_BLUR_SIZE, 1f,
                        IParams.PARAM_NONE
                ))
            }
    private val highPassFilter = HighPassFilter(normalFilter.frameBufferTexture)
    private val afterHorizontalBlurFilter = BlurDirectionFilter()
            .apply {
                setParams(floatArrayOf(
                        BlurDirectionFilter.PARAM_DIRECTION, 0f,
                        BlurDirectionFilter.PARAM_BLUR_SIZE, 2f,
                        IParams.PARAM_NONE
                ))
            }
    private val afterVerticalBlurFilter = BlurDirectionFilter()
            .apply {
                setParams(floatArrayOf(
                        BlurDirectionFilter.PARAM_DIRECTION, 1f,
                        BlurDirectionFilter.PARAM_BLUR_SIZE, 2f,
                        IParams.PARAM_NONE
                ))
            }
    private val resultFilter = ResultFilter(normalFilter.frameBufferTexture, verticalBlurFilter.frameBufferTexture)

    init {
        addFilter(horizontalBlurFilter)
        addFilter(verticalBlurFilter)
        addFilter(highPassFilter)
        addFilter(afterHorizontalBlurFilter)
        addFilter(afterVerticalBlurFilter)
        addFilter(resultFilter)
    }

    companion object {
        fun create(): BaseFilter = BeautyV5Filter(NormalFilter())
    }

    class ResultFilter(var srcTextureId: IntArray,
                       var srcBlurTextureId: IntArray,
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
                    Texture(srcTextureId, "sTexture2"),
                    Texture(srcBlurTextureId, "sTexture3")
            )
        }

        override fun getVertex(): String {
            return "shader/vertex_normal.glsl"
        }

        override fun getFragment(): String {
            return "shader/fragment_result.glsl"
        }
    }
}