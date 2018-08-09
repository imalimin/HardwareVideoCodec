package com.lmy.codec.presenter

import android.graphics.SurfaceTexture
import android.view.TextureView
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.texture.impl.filter.BaseFilter

/**
 * Created by lmyooyo@gmail.com on 2018/8/9.
 */
interface VideoRecorder : SurfaceTexture.OnFrameAvailableListener {
    fun prepare()
    fun start()
    fun pause()
    fun stop()
    fun reset()
    fun prepared(): Boolean
    fun started(): Boolean
    fun enableHardware(enable: Boolean)
    fun setOutputSize(width: Int, height: Int)
    fun getWidth(): Int
    fun getHeight(): Int
    fun setFilter(filter: Class<*>)
    fun getFilter(): BaseFilter?
    /**
     * File or rtmp url
     */
    fun setOutputUri(uri: String)

    fun setPreviewDisplay(view: TextureView)
    fun setOnStateListener(listener: OnStateListener)
    fun release()
    interface OnStateListener : Encoder.OnPreparedListener, Encoder.OnRecordListener {
        fun onStop()
    }
}