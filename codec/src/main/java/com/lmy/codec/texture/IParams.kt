package com.lmy.codec.texture

/**
 * Created by lmyooyo@gmail.com on 2018/9/28.
 */
interface IParams {
    companion object {
        const val PARAM_NONE = Int.MIN_VALUE.toFloat()
    }

    fun setParams(params: FloatArray)
    fun setParams(cursor: Float, value: Float)
}