package com.lmy.codec.helper

class FpsMeasurer private constructor(private var intervalMs: Int) {
    companion object {
        fun create(intervalMs: Int = 3000): FpsMeasurer {
            return FpsMeasurer(intervalMs)
        }
    }

    var onUpdateListener: OnUpdateListener? = null
    private var startTime = 0L
    private var total = 0L
    private var count = 0
    private var start = 0L
    private var fps = 0f
        get() = if (0f == field) calculate() else field

    private fun calculate(): Float = count / total.toFloat() * 1000

    fun start() {
        synchronized(this) {
            if (startTime <= 0) {
                startTime = System.currentTimeMillis()
            }
            start = System.currentTimeMillis()
        }
    }

    fun end() {
        synchronized(this) {
            if (startTime <= 0) return
            val delta = System.currentTimeMillis() - start
            if (delta < 0) return
            total += delta
            ++count
            if (System.currentTimeMillis() - startTime > intervalMs) {
                fps = calculate()
                startTime = 0
                total = 0
                count = 0
                onUpdateListener?.onUpdate(this, fps)
            }
        }
    }

    fun reset() {
        startTime = 0
        total = 0
        count = 0
        start = 0L
        fps = 0f
    }

    interface OnUpdateListener {
        fun onUpdate(measurer: FpsMeasurer, fps: Float)
    }
}