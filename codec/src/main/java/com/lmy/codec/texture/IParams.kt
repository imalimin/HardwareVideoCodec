package com.lmy.codec.texture

/**
 * Created by lmyooyo@gmail.com on 2018/9/28.
 */
interface IParams {
    companion object {
        const val PARAM_NONE = 0f
    }

    fun setParams(params: FloatArray)
    fun setParam(cursor: Float, value: Float)
}