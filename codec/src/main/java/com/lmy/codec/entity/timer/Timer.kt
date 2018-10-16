package com.lmy.codec.entity.timer

/**
 * Created by lmyooyo@gmail.com on 2018/10/16.
 */
interface Timer {
    /**
     * @return ns
     */
    fun get(): Long

    fun reset()
    fun start()
    fun pause()
}