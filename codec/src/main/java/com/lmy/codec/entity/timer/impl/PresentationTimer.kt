package com.lmy.codec.entity.timer.impl

import com.lmy.codec.entity.timer.Timer

/**
 * Created by lmyooyo@gmail.com on 2018/10/16.
 */
class PresentationTimer : Timer {
    private var startTime = 0L
    private var waitCountTime = 0L
    private var pauseTime = 0L
    private var running = false

    /**
     * @return ns
     */
    @Synchronized
    override fun get(): Long {
        return now() - startTime - waitCountTime
    }

    @Synchronized
    override fun reset() {
        running = false
        startTime = 0
        waitCountTime = 0
        pauseTime = 0
    }

    @Synchronized
    override fun start() {
        if (running) return
        running = true
        if (pauseTime > 0) {
            waitCountTime += now() - pauseTime
        }
        if (startTime <= 0) {
            startTime = now()
        }
    }

    @Synchronized
    override fun pause() {
        if (!running) return
        running = false
        pauseTime = now()
    }

    @Synchronized
    private fun now(): Long {
        return System.nanoTime()
    }
}