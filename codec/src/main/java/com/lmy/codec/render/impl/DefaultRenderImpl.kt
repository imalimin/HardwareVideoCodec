/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.render.impl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.helper.FpsMeasurer
import com.lmy.codec.helper.PixelsReader
import com.lmy.codec.pipeline.impl.GLEventPipeline
import com.lmy.codec.render.Render
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.texture.impl.filter.NormalFilter
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.ScreenTextureWrapper


/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class DefaultRenderImpl(var context: CodecContext,
                        var cameraWrapper: CameraTextureWrapper,
                        var transformMatrix: FloatArray = FloatArray(16),
                        var screenTexture: SurfaceTexture? = null,
                        var screenWrapper: ScreenTextureWrapper? = null,
                        var reader: PixelsReader? = null)
    : Render, FpsMeasurer.OnUpdateListener {

    private val filterLock = Any()
    private var filter: BaseFilter? = null
    private var width: Int = 0
    private var height: Int = 0
    private val videoMeasurer: FpsMeasurer = FpsMeasurer.create().apply {
        onUpdateListener = this@DefaultRenderImpl
    }
    private val renderMeasurer: FpsMeasurer = FpsMeasurer.create().apply {
        onUpdateListener = this@DefaultRenderImpl
    }

    fun init() {
        this.width = context.video.width
        this.height = context.video.height
        initReader()
        initFilter(if (null != filter) filter!! else NormalFilter())
    }

    private fun initFilter(f: BaseFilter) {
        synchronized(filterLock) {
            cameraWrapper.egl?.makeCurrent()
            filter?.release()
            filter = f
            filter?.width = this.width
            filter?.height = this.height
            debug_e("camera texture: ${cameraWrapper.getFrameBuffer()[0]}, ${cameraWrapper.getFrameBufferTexture()[0]}")
            filter?.textureId = cameraWrapper.getFrameBufferTexture()
            filter?.init()
        }
        initScreen()
    }

    private fun initReader() {
//        reader?.stop()
//        reader = PixelsReader.create(Resources.instance.isSupportPBO(), this.width, this.height)
//        reader?.start()
    }

    private fun initScreen() {
        if (null == screenWrapper) {
            screenWrapper = ScreenTextureWrapper(screenTexture, getFrameBufferTexture(),
                    cameraWrapper.egl!!.eglContext!!)
        }
        screenWrapper?.egl?.makeCurrent()
        screenWrapper?.updateLocation(context)
    }

    override fun draw() {
        if (null == screenWrapper) return
        videoMeasurer.end()
        videoMeasurer.start()
        renderMeasurer.start()
        drawCamera()
        drawFilter()
        screenWrapper?.egl?.makeCurrent()
        GLES20.glViewport(0, 0, context.viewSize.width, context.viewSize.height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        screenWrapper?.draw(transformMatrix)
        screenWrapper?.egl?.swapBuffers()
        renderMeasurer.end()
    }

    private fun drawFilter() {
        synchronized(filterLock) {
            cameraWrapper.egl?.makeCurrent()
            GLES20.glViewport(0, 0, this.width, this.height)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
            filter?.draw(null)
//            ++count
//            if (0 == count % 60) {
//                reader?.readPixels(filter!!.frameBuffer[0])
//                reader?.shoot("${Environment.getExternalStorageDirectory().path}/temp.jpg")
//                reader?.recycleBuffer()
//            }
        }
    }

    private fun drawCamera() {
        cameraWrapper.egl?.makeCurrent()
        if (null != cameraWrapper.surfaceTexture) {
            cameraWrapper.surfaceTexture?.updateTexImage()
            cameraWrapper.surfaceTexture?.getTransformMatrix(transformMatrix)
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        cameraWrapper.draw(transformMatrix)
    }

    override fun start(texture: SurfaceTexture, width: Int, height: Int) {
        updateScreenTexture(texture)
        context.viewSize.width = width
        context.viewSize.height = height
        GLEventPipeline.INSTANCE.queueEvent(Runnable { init() })
    }

    override fun updateSize(width: Int, height: Int) {
        if (width == this.width && this.height == height) return
        this.width = width
        this.height = height
        GLEventPipeline.INSTANCE.queueEvent(Runnable {
            cameraWrapper.egl?.makeCurrent()
            initReader()
            cameraWrapper.updateLocation(context)
        })
        GLEventPipeline.INSTANCE.queueEvent(Runnable {
            synchronized(filterLock) {
                cameraWrapper.egl?.makeCurrent()
                filter?.updateFrameBuffer(this.width, this.height)
            }
        })
        GLEventPipeline.INSTANCE.queueEvent(Runnable {
            initScreen()
        })
    }

    override fun stop() {
        GLEventPipeline.INSTANCE.queueEvent(Runnable {
            cameraWrapper.egl?.makeCurrent()
            BaseFilter.release()
            screenWrapper?.release()
            screenWrapper = null
            debug_i("release")
        })
    }

    override fun release() {
        reader?.stop()
        stop()
    }

    override fun onFrameAvailable() {
        GLEventPipeline.INSTANCE.queueEvent(Runnable { draw() })
    }

    fun updateScreenTexture(texture: SurfaceTexture?) {
        screenTexture = texture
    }

    override fun post(runnable: Runnable) {
        GLEventPipeline.INSTANCE.queueEvent(runnable)
    }

    override fun setFilter(filter: BaseFilter) {
        GLEventPipeline.INSTANCE.queueEvent(Runnable {
            initFilter(filter)
        })
    }

    override fun getFilter(): BaseFilter? {
        synchronized(filterLock) {
            return filter
        }
    }

    override fun getFrameBuffer(): IntArray {
        synchronized(filterLock) {
            if (null != filter) return filter!!.frameBuffer
        }
        return cameraWrapper.getFrameBuffer()

    }

    override fun getFrameBufferTexture(): IntArray {
        synchronized(filterLock) {
            if (null != filter) return filter!!.frameBufferTexture
        }
        return cameraWrapper.getFrameBufferTexture()
    }

    override fun onUpdate(measurer: FpsMeasurer, fps: Float) {
        if (measurer == videoMeasurer) {
            debug_i("Video fps $fps")
        } else {
            debug_i("Render fps $fps")
        }
    }
}