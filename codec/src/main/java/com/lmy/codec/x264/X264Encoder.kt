package com.lmy.codec.x264

import android.media.MediaFormat

/**
 * Created by lmyooyo@gmail.com on 2018/4/3.
 */
class X264Encoder(private var format: MediaFormat) {
    init {
        System.loadLibrary("x264")
        System.loadLibrary("codec")
        init()
        setVideoSize(format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT))
        setBitrate(format.getInteger(MediaFormat.KEY_BIT_RATE))
        setFrameFormat(FrameFormat.X264_CSP_RGB)
        setFps(format.getInteger(MediaFormat.KEY_FRAME_RATE))
    }

    private external fun init()
    external fun start()
    external fun stop()
    external fun encode(src: ByteArray, srcSize: Int, out: ByteArray): Int
    external fun setVideoSize(width: Int, height: Int)
    external fun setBitrate(bitrate: Int)
    external fun setFrameFormat(format: Int)
    external fun setFps(fps: Int)
    external fun setProfile(profile: String)
    external fun setLevel(level: Int)
}