/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.render.impl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import com.lmy.codec.entity.Parameter
import com.lmy.codec.pipeline.SingleEventPipeline
import com.lmy.codec.render.Render
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.texture.impl.filter.NormalFilter
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.ScreenTextureWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class DefaultRenderImpl(var parameter: Parameter,
                        var cameraWrapper: CameraTextureWrapper,
                        var transformMatrix: FloatArray = FloatArray(16),
                        var screenTexture: SurfaceTexture? = null,
                        var screenWrapper: ScreenTextureWrapper? = null,
                        var width: Int = 1,
                        var height: Int = 1,
                        private var viewportX: Int = 0,
                        private var viewportY: Int = 0,
                        var cameraWidth: Int = 0,
                        var cameraHeight: Int = 0)
    : Render {

    private val filterLock = Any()
    private lateinit var filter: BaseFilter

    fun init() {
        initFilter(NormalFilter::class.java)
    }

    private fun initFilter(clazz: Class<*>) {
        synchronized(filterLock) {
            try {
                filter = clazz.newInstance() as BaseFilter
            } catch (e: Exception) {
                e.printStackTrace()
                initScreen()
                return
            }
            filter.width = parameter.video.width
            filter.height = parameter.video.height
            filter.textureId = cameraWrapper.getFrameBufferTexture()
            filter.init()
        }
        initScreen()
    }

    private fun initScreen() {
        if (null == screenWrapper) {
            screenWrapper = ScreenTextureWrapper(screenTexture, getFrameBufferTexture(),
                    cameraWrapper.egl!!.eglContext!!)
        }
    }

    override fun draw() {
        if (null == screenWrapper) return
        drawCamera()
        drawFilter()
        screenWrapper?.egl?.makeCurrent()
        GLES20.glViewport(viewportX, viewportY, width, height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        screenWrapper?.drawTexture(transformMatrix)
        screenWrapper?.egl?.swapBuffers()
    }

    private fun drawFilter() {
        synchronized(filterLock) {
            GLES20.glViewport(0, 0, parameter.video.width, parameter.video.height)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            filter.drawTexture(null)
        }
    }

    private fun drawCamera() {
        if (null != cameraWrapper.surfaceTexture) {
            cameraWrapper.surfaceTexture?.updateTexImage()
            cameraWrapper.surfaceTexture?.getTransformMatrix(transformMatrix)
        }
        cameraWrapper.egl?.makeCurrent("cameraWrapper")
        GLES20.glViewport(0, 0, parameter.previewHeight, parameter.previewWidth)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        cameraWrapper.drawTexture(transformMatrix)
    }

    override fun start(texture: SurfaceTexture, width: Int, height: Int) {
        updateScreenTexture(texture)
        initViewport(width, height)
        SingleEventPipeline.instance.queueEvent(Runnable { init() })
    }

    private fun initViewport(width: Int, height: Int) {
//        this.width = width
//        this.height = height
        initCameraViewport(width, height)
        debug_e("initViewport($width, $height): before")
        val videoRatio = parameter.video.width / parameter.video.height.toFloat()
        val viewRatio = width / height.toFloat()
        if (videoRatio > viewRatio) {//以View的宽为准
            this.width = width
            this.height = (width / videoRatio).toInt()
        } else {//以View的高为准
            this.width = (height * videoRatio).toInt()
            this.height = height
        }
        viewportX = (width - this.width) / 2
        viewportY = (height - this.height) / 2
        debug_e("initViewport(${this.viewportX}, ${this.viewportY})(${this.width}, ${this.height}): after")
    }

    private fun initCameraViewport(width: Int, height: Int) {
        cameraWidth = width
        cameraHeight = height
        //摄像头宽高以横屏为准
        val cameraRatio = parameter.previewHeight / parameter.previewWidth.toFloat()
        val viewRatio = width / height.toFloat()
        if (cameraRatio < viewRatio) {//高度被压缩了，以View的宽为准
            this.cameraWidth = width
            this.cameraHeight = (width / cameraRatio).toInt()
        } else {
            this.cameraWidth = (height * cameraRatio).toInt()
            this.cameraHeight = height
        }
        debug_e("initCameraViewport(${this.cameraWidth}, ${this.cameraHeight})")
    }

    override fun stop() {
        SingleEventPipeline.instance.queueEvent(Runnable {
            screenWrapper?.release()
            filter.release()
            screenWrapper = null
        })
    }

    override fun release() {
        stop()
    }

    override fun onFrameAvailable() {
        SingleEventPipeline.instance.queueEvent(Runnable { draw() })
    }

    fun updateScreenTexture(texture: SurfaceTexture?) {
        screenTexture = texture
    }

    override fun post(runnable: Runnable) {
        SingleEventPipeline.instance.queueEvent(runnable)
    }

    override fun setFilter(filter: Class<*>) {
        SingleEventPipeline.instance.queueEvent(Runnable {
            initFilter(filter)
        })
    }

    override fun getFilter(): BaseFilter {
        synchronized(filterLock) {
            return filter
        }
    }

    override fun getFrameBuffer(): Int {
        synchronized(filterLock) {
            if (null != filter) return filter!!.frameBuffer!!
        }
        return cameraWrapper.getFrameBuffer()

    }

    override fun getFrameBufferTexture(): Int {
        synchronized(filterLock) {
            if (null != filter) return filter!!.frameBufferTexture!!
        }
        return cameraWrapper.getFrameBufferTexture()
    }
}