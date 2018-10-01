package com.lmy.codec.helper

import android.graphics.PixelFormat
import android.media.ImageReader
import android.opengl.EGLContext
import android.opengl.GLES20
import android.util.Log
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.wrapper.CodecTextureWrapper
import java.nio.ByteBuffer

class SurfacePixelsReader private constructor(private var width: Int,
                                              private var height: Int,
                                              private var textureId: IntArray,
                                              private var eglContext: EGLContext,
                                              private var maxImages: Int = 5)
    : ImageReader.OnImageAvailableListener {

    private var imageReader: ImageReader = ImageReader.newInstance(width, height,
            PixelFormat.RGBA_8888, maxImages)
    private var codecWrapper: CodecTextureWrapper? = null
    private var mPipeline: Pipeline = EventPipeline.create("SurfacePixelsReader")
    private var data: ByteArray? = null
    var onReadListener: OnReadListener? = null

    init {
        imageReader.setOnImageAvailableListener(this, mPipeline.getHandler())
        data = ByteArray(width * height * 4)
    }

    fun prepare() {
        codecWrapper = CodecTextureWrapper(imageReader.surface, textureId, eglContext)
    }

    fun read() {
        codecWrapper?.egl?.makeCurrent()
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        codecWrapper?.draw(null)
        codecWrapper?.egl?.swapBuffers()
    }

    fun stop() {
        codecWrapper?.release()
        codecWrapper = null
        imageReader.close()
        onReadListener = null
    }

    override fun onImageAvailable(reader: ImageReader) {
        Log.e("SurfacePixelsReader", "read")
        val image = reader.acquireNextImage()
        val planes = image.planes
        val width = image.width
        val rowStride = planes[0].rowStride
        val pixelStride = planes[0].pixelStride
        val rowPadding = rowStride - pixelStride * width
        copyToByteArray(planes[0].buffer, rowPadding)
        image?.close()
    }

    private fun copyToByteArray(buffer: ByteBuffer, rowPadding: Int) {
        if (null != data && null != onReadListener) {
            GLHelper.copyToByteArray(buffer, data!!, width, height * 4, rowPadding)
            onReadListener?.onRead(data!!)
        }
    }

    companion object {
        fun build(width: Int, height: Int, textureId: IntArray, eglContext: EGLContext): SurfacePixelsReader {
            return SurfacePixelsReader(width, height, textureId, eglContext)
        }
    }

    interface OnReadListener {
        fun onRead(data: ByteArray)
    }
}