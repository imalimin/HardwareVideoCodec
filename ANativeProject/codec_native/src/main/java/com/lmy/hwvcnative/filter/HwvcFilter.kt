package com.lmy.hwvcnative.filter

class HwvcFilter(path: String) : Filter() {

    init {
        handler = create(path)
    }

    override fun setParams(params: IntArray) {
        if (0L == handler) return
        setParams(handler, params)
    }

    override fun setParam(index: Int, value: Int) {

    }

    private external fun create(path: String): Long
    private external fun setParams(handler: Long, params: IntArray)
}