package com.lmy.hwvcnative.filter

class NormalFilter : Filter() {

    init {
        handler = create()
    }

    override fun setParams(params: IntArray) {
        if (0L == handler) return
        setParams(handler, params)
    }

    private external fun create(): Long
    private external fun setParams(handler: Long, params: IntArray)
}