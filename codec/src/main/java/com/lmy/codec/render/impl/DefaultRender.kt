package com.lmy.codec.render.impl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.render.Render
import com.lmy.codec.texture.impl.NormalTexture
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.ScreenTextureWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class DefaultRender(var cameraWrapper: CameraTextureWrapper,
                    var transformMatrix: FloatArray = FloatArray(16),
                    var screenTexture: SurfaceTexture? = null,
                    var screenWrapper: ScreenTextureWrapper? = null,
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
                        screenWrapper?.release()
                    }
                }
            }
        }
    }

    fun init() {
        cameraWrapper.initEGL(width, height)
        screenWrapper = ScreenTextureWrapper(screenTexture, cameraWrapper.egl!!.eglContext!!)
        screenWrapper?.setFilter(NormalTexture(cameraWrapper.getFrameTexture(), cameraWrapper.getDrawer()))
//        (screenWrapper!!.texture as BeautyTexture).setParams(0f, -5f)//beauty: 0 - 2.5, tone: -5 - 5
//        (screenWrapper!!.texture as BeautyTexture).setBrightLevel(0f)//0 - 1
//        (screenWrapper!!.texture as BeautyTexture).setTexelOffset(-10f)//-10 - 10
    }

    override fun draw() {
        drawCamera()
        screenWrapper?.egl?.makeCurrent()
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        screenWrapper?.drawTexture(transformMatrix)
        screenWrapper?.egl?.swapBuffers()
    }

    private fun drawCamera() {
        if (null != cameraWrapper.surfaceTexture) {
            cameraWrapper.surfaceTexture?.updateTexImage()
            cameraWrapper.surfaceTexture?.getTransformMatrix(transformMatrix)
        }
        cameraWrapper.egl?.makeCurrent("cameraWrapper")
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        cameraWrapper.drawTexture(transformMatrix)
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