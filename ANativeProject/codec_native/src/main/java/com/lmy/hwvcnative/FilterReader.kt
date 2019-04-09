package com.lmy.hwvcnative

class FilterReader(path: String) : CPPObject() {
    init {
        handler = create(path)
    }

    fun read() {
        if (0L == handler) return
        read(handler)
    }

    private external fun create(path: String): Long
    private external fun read(handler: Long): Long
}