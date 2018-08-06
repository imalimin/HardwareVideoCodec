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
import com.lmy.codec.helper.PixelsReader
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
class DefaultRenderImpl(var context: CodecContext,
                        var cameraWrapper: CameraTextureWrapper,
                        var transformMatrix: FloatArray = FloatArray(16),
                        var screenTexture: SurfaceTexture? = null,
                        var screenWrapper: ScreenTextureWrapper? = null,
                        var reader: PixelsReader? = null)
    : Render {

    private val filterLock = Any()
    private var filter: BaseFilter? = null

    fun init() {
        initReader()
        initFilter(NormalFilter::class.java)
    }

    private fun initFilter(clazz: Class<*>) {
        synchronized(filterLock) {
            //由于使用共享的FBO，所以更换filter的时候不能释放
//            filter?.release()
            try {
                filter = clazz.newInstance() as BaseFilter
            } catch (e: Exception) {
                e.printStackTrace()
                initScreen()
                return
            }
            filter?.width = context.video.width
            filter?.height = context.video.height
            debug_e("camera texture: ${cameraWrapper.getFrameBuffer()[0]}, ${cameraWrapper.getFrameBufferTexture()[0]}")
            filter?.textureId = cameraWrapper.getFrameBufferTexture()
            filter?.init()
        }
        initScreen()
    }

    private fun initReader() {
        reader?.stop()
        reader = PixelsReader.create(context.supportPBO, context.video.width, context.video.height)
        reader?.start()
    }

    private fun initScreen() {
        if (null == screenWrapper) {
            screenWrapper = ScreenTextureWrapper(screenTexture, getFrameBufferTexture(),
                    cameraWrapper.egl!!.eglContext!!)
        }
        screenWrapper?.updateLocation(context)
    }

    override fun draw() {
        if (null == screenWrapper) return
        drawCamera()
        drawFilter()
        screenWrapper?.egl?.makeCurrent()
        GLES20.glViewport(0, 0, context.viewSize.width, context.viewSize.height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        screenWrapper?.drawTexture(transformMatrix)
        screenWrapper?.egl?.swapBuffers()
    }

    private fun drawFilter() {
        synchronized(filterLock) {
            GLES20.glViewport(0, 0, context.video.width, context.video.height)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
            filter?.drawTexture(null)
//            ++count
//            if (0 == count % 60) {
//                reader?.readPixels(filter!!.frameBuffer[0])
//                reader?.shoot("${Environment.getExternalStorageDirectory().path}/temp.jpg")
//                reader?.recycleBuffer()
//            }
        }
    }

    private fun drawCamera() {
        if (null != cameraWrapper.surfaceTexture) {
            cameraWrapper.surfaceTexture?.updateTexImage()
            cameraWrapper.surfaceTexture?.getTransformMatrix(transformMatrix)
        }
        cameraWrapper.egl?.makeCurrent()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        cameraWrapper.drawTexture(transformMatrix)
    }

    private var count = 0

    override fun start(texture: SurfaceTexture, width: Int, height: Int) {
        updateScreenTexture(texture)
        context.viewSize.width = width
        context.viewSize.height = height
        SingleEventPipeline.instance.queueEvent(Runnable { init() })
    }

    override fun updateSize(width: Int, height: Int) {
        context.video.width = width
        context.video.height = height
        SingleEventPipeline.instance.queueEvent(Runnable {
            cameraWrapper.updateLocation(context)
            initReader()
            synchronized(filterLock) {
                filter?.updateFrameBuffer(context.video.width, context.video.height)
            }
            initScreen()
        })
    }

    override fun stop() {
        SingleEventPipeline.instance.queueEvent(Runnable {
            screenWrapper?.release()
            screenWrapper = null
            filter?.release()
        })
    }

    override fun release() {
        reader?.stop()
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
}