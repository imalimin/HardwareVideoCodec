package com.lmy.codec.texture.impl

/**
 * Created by lmyooyo@gmail.com on 2018/4/25.
 */
abstract class BaseTextureFilter(width: Int = 0,
                                 height: Int = 0,
                                 textureId: Int = -1) : BaseFrameBufferTexture(width, height, textureId){
    abstract fun init()
}