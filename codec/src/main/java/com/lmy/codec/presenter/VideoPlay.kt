package com.lmy.codec.presenter

import android.view.TextureView
import java.io.File

interface VideoPlay : FilterSupport {
    fun reset()
    fun prepare()
    fun setInputResource(file: File)
    fun setPreviewDisplay(view: TextureView)
    fun start()
    fun pause()
    fun stop()
    fun release()
}