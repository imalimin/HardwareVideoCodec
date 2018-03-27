package com.lmy.codec.render.impl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.render.Render
import com.lmy.codec.texture.impl.NormalTexture
import com.lmy.codec.wrapper.ScreenTextureWrapper
import com.lmy.codec.wrapper.TextureWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class DefaultRender(var cameraWrapper: TextureWrapper,
                    var screenTexture: SurfaceTexture? = null,
                    var width: Int = 1,
                    var height: Int = 1)
    : Render {

    companion object {
        val INIT = 0x1
        val RENDER = 0x2
        val STOP = 0x3
    }

    private var mHandlerThread = HandlerThread("Renderer_Thread")
    private var mHandler: Handler? = null
    private var mScreenWrapper: ScreenTextureWrapper? = null
    private var transformMatrix = FloatArray(16)

    init {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    INIT -> {
                        init()
                    }
                    RENDER -> {
                        draw()
                    }
                    STOP -> {
                        mScreenWrapper?.release()
                    }
                }
            }
        }
    }

    fun init() {
        mScreenWrapper = ScreenTextureWrapper(screenTexture)
        mScreenWrapper?.setFilter(NormalTexture(cameraWrapper.textureId!!))
        mScreenWrapper?.egl?.makeCurrent()
    }

    override fun draw() {
        if (null != cameraWrapper.surfaceTexture) {
            cameraWrapper.surfaceTexture?.updateTexImage()
            cameraWrapper.surfaceTexture?.getTransformMatrix(transformMatrix)
        }
        mScreenWrapper?.egl?.makeCurrent()
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        mScreenWrapper?.drawTexture(transformMatrix)
        mScreenWrapper?.egl?.swapBuffers()
    }

    override fun start(texture: SurfaceTexture, width: Int, height: Int) {
        updateScreenTexture(texture)
        this.width = width
        this.height = height
        mHandler?.sendEmptyMessage(INIT)
    }

    override fun stop() {
        mHandler?.sendEmptyMessage(STOP)
    }

    override fun release() {
        stop()
    }

    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
        mHandler?.sendEmptyMessage(RENDER)
    }

    fun updateScreenTexture(texture: SurfaceTexture?) {
        screenTexture = texture
    }
}