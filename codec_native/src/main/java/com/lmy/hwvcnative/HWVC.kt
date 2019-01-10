package com.lmy.hwvcnative

import android.content.Context

class HWVC {
    companion object {
        @Synchronized
        fun init(context: Context) {
            System.loadLibrary("avcodec")
            System.loadLibrary("avformat")
            System.loadLibrary("avresample")
            System.loadLibrary("avutil")
            System.loadLibrary("swresample")
            System.loadLibrary("hwvcom")
            System.loadLibrary("hwvc_codec")
            System.loadLibrary("hwvc_render")
            System.loadLibrary("hwvc_native")
        }
    }
}