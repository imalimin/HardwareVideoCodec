/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.presenter.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLUtils
import android.view.TextureView
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.presenter.ImageProcessor
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.texture.impl.filter.NormalFilter
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_i
import com.lmy.codec.wrapper.ScreenTextureWrapper
import java.io.File
import javax.microedition.khronos.opengles.GL10

/**
 * Created by lmyooyo@gmail.com on 2018/9/21.
 */
class ImageProcessorImpl private constructor(ctx: Context) : ImageProcessor {
    companion object {
        fun create(ctx: Context): ImageProcessor = ImageProcessorImpl(ctx)
    }

    private val filterLock = Any()
    private val context: CodecContext = CodecContext(ctx)
    private val transformMatrix: FloatArray = floatArrayOf(
            0.0f, -1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f
    )
    private var textureView: TextureView? = null
    private var filter: BaseFilter? = null
    private var mPipeline: Pipeline = EventPipeline.create("ImageProcessor")
    private var srcInputTexture = IntArray(1)
    private var screenInputTexture = IntArray(1)
    private var screenTexture: SurfaceTexture? = null
    private var screenWrapper: ScreenTextureWrapper? = null
    private var isPrepare = false

    private fun createEGL() {
        debug_i("createEGL")
        if (null == screenWrapper) {
            screenWrapper = ScreenTextureWrapper(screenTexture, screenInputTexture, null)
        }
        screenWrapper?.updateLocation(context)
    }

    private fun createSrcTexture() {
        debug_i("createSrcTexture")
        screenWrapper?.egl?.makeCurrent()
        GLES20.glGenTextures(srcInputTexture.size, srcInputTexture, 0)
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, srcInputTexture[0])
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, GLES20.GL_NONE)
    }

    private fun createFilter(clazz: Class<*>) {
        debug_i("createFilter")
        screenWrapper?.egl?.makeCurrent()
        synchronized(filterLock) {
            filter?.release()
            try {
                filter = clazz.newInstance() as BaseFilter
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            filter?.width = context.video.width
            filter?.height = context.video.height
            filter?.textureId = srcInputTexture
            filter?.init()
            screenInputTexture[0] = filter!!.frameBufferTexture[0]
        }
    }

    private fun invalidate() {
        synchronized(filterLock) {
            filter?.drawTexture(null)
        }
        GLES20.glViewport(0, 0, context.viewSize.width, context.viewSize.height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        screenWrapper?.drawTexture(null)
        screenWrapper?.egl?.swapBuffers()
        debug_i("invalidate")
    }

    private fun updateSrcTexture(bitmap: Bitmap) {
        context.video.width = bitmap.width
        context.video.height = bitmap.height
        debug_i("updateSrcTexture ${context.video.width}x${context.video.height}, " +
                "${context.viewSize.width}x${context.viewSize.height}, " +
                "${srcInputTexture[0]}")
        screenWrapper?.egl?.makeCurrent()
        screenWrapper?.updateLocation(context)
        synchronized(filterLock) {
            filter?.updateFrameBuffer(context.video.width, context.video.height)
        }
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, srcInputTexture[0])
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, GLES20.GL_NONE)
        bitmap.recycle()
        invalidate()
    }

    private var featureFile: File? = null
    override fun setInputImage(file: File) {
        if (!file.exists()) {
            debug_e("Input file is not exists")
            return
        }
        if (!isPrepare) {
            featureFile = file
            return
        }
        mPipeline.queueEvent(Runnable {
            updateSrcTexture(BitmapFactory.decodeFile(file.absolutePath))
        })
    }

    override fun prepare() {
        mPipeline.queueEvent(Runnable {
            createPreview()
        })
    }

    private fun prepareOpenGL(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        context.video.width = 32
        context.video.height = 32
        screenTexture = surfaceTexture
        context.viewSize.width = width
        context.viewSize.height = height
        mPipeline.queueEvent(Runnable {
            createEGL()
            createSrcTexture()
            createFilter(NormalFilter::class.java)
            isPrepare = true
            if (null != featureFile) {
                setInputImage(featureFile!!)
                featureFile = null
            }
        })
    }

    private fun updatePreview(width: Int, height: Int) {
        if (null == screenWrapper) return
        context.viewSize.width = width
        context.viewSize.height = height
        screenWrapper?.updateLocation(context)
    }

    override fun setPreviewDisplay(view: TextureView) {
        this.textureView = view
    }

    override fun setFilter(filter: Class<*>) {
        mPipeline.queueEvent(Runnable {
            createFilter(filter)
            invalidate()
        })
    }

    override fun getFilter(): BaseFilter? {
        synchronized(filterLock) {
            return filter
        }
    }

    override fun release() {
        mPipeline.queueEvent(Runnable {
            screenWrapper?.egl?.makeCurrent()
            GLES20.glDeleteTextures(srcInputTexture.size, srcInputTexture, 0)
            synchronized(filterLock) {
                filter?.release()
                screenWrapper?.release()
            }
        })
        mPipeline.quit()
        context.release()
    }

    private fun createPreview() {
        if (null != textureView && textureView!!.isAvailable) {
            mPipeline.queueEvent(Runnable {
                prepareOpenGL(textureView!!.surfaceTexture, textureView!!.width, textureView!!.height)
            })
            return
        }
        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {
                updatePreview(p1, p2)
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
//                stop()
                return true
            }

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                if (null != p0) {
                    mPipeline.queueEvent(Runnable {
                        prepareOpenGL(p0, p1, p2)
                    })
                }
                debug_e("onSurfaceTextureAvailable")
            }
        }
    }
}