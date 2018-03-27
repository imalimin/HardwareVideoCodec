package com.lmy.codec

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.ScreenTextureWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class Render(var screenTexture: SurfaceTexture? = null,
             var width: Int = 1,
             var height: Int = 1)
    : SurfaceTexture.OnFrameAvailableListener {

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

                    }
                }
            }
        }
    }

    fun init() {
        mScreenWrapper = ScreenTextureWrapper(screenTexture)
        mScreenWrapper?.egl?.makeCurrent()
    }

    fun draw() {
        if (null != CameraTextureWrapper.instance.surfaceTexture) {
            CameraTextureWrapper.instance.surfaceTexture?.updateTexImage()
            CameraTextureWrapper.instance.surfaceTexture?.getTransformMatrix(transformMatrix)
        }
        debug_e("draw")
        mScreenWrapper?.egl?.makeCurrent()
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        drawTexture()
        mScreenWrapper?.egl?.swapBuffers()
    }

    private var aPositionLocation = 0
    private var aTextureCoordLocation = 0
    private var uTextureMatrixLocation = 0
    private var uTextureSamplerLocation = 0

    private fun drawTexture() {
        aPositionLocation = mScreenWrapper!!.getPositionLocation()
        aTextureCoordLocation = mScreenWrapper!!.getTextureCoordinateLocation()
        uTextureMatrixLocation = mScreenWrapper!!.getTextureMatrixLocation()
        uTextureSamplerLocation = mScreenWrapper!!.getTextureSamplerLocation()

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                CameraTextureWrapper.instance.textureId!!)
        GLES20.glUniform1i(uTextureSamplerLocation, 0)
        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0)

        if (null != mScreenWrapper!!.buffer) {
            mScreenWrapper!!.buffer!!.position(0)
            GLES20.glEnableVertexAttribArray(aPositionLocation)
            GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 16, mScreenWrapper!!.buffer)

            mScreenWrapper!!.buffer!!.position(2)
            GLES20.glEnableVertexAttribArray(aTextureCoordLocation)
            GLES20.glVertexAttribPointer(aTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 16, mScreenWrapper!!.buffer)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        }
    }

    fun start(texture: SurfaceTexture, width: Int, height: Int) {
        updateScreenTexture(texture)
        this.width = width
        this.height = height
        mHandler?.sendEmptyMessage(INIT)
    }

    fun stop() {
        mHandler?.sendEmptyMessage(STOP)
    }

    fun release() {
        stop()
    }

    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
        mHandler?.sendEmptyMessage(RENDER)
    }

    fun updateScreenTexture(texture: SurfaceTexture?) {
        screenTexture = texture
    }
}