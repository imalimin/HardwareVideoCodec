/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.x264

/**
 * Match x264.h
 * Created by lmyooyo@gmail.com on 2018/4/3.
 */
class FrameFormat {
    companion object {
        val X264_CSP_MASK = 0x00ff
        val X264_CSP_NONE = 0x0000
        val X264_CSP_I420 = 0x0001
        val X264_CSP_YV12 = 0x0002
        val X264_CSP_NV12 = 0x0003
        val X264_CSP_NV21 = 0x0004
        val X264_CSP_I422 = 0x0005
        val X264_CSP_YV16 = 0x0006
        val X264_CSP_NV16 = 0x0007
        val X264_CSP_YUYV = 0x0008
        val X264_CSP_UYVY = 0x0009
        val X264_CSP_V210 = 0x000a
        val X264_CSP_I444 = 0x000b
        val X264_CSP_YV24 = 0x000c
        val X264_CSP_BGR = 0x000d
        val X264_CSP_BGRA = 0x000e
        val X264_CSP_RGB = 0x000f
        val X264_CSP_MAX = 0x0010
        val X264_CSP_VFLIP = 0x1000
        val X264_CSP_HIGH_DEPTH = 0x2000
    }
}