package com.lmy.codec.encoder.impl

import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.EGLContext
import android.opengl.GLES20
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.PresentationTimer
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CodecTextureWrapper
import com.lmy.codec.x264.CacheX264Encoder
import com.lmy.codec.x264.X264Encoder
import java.nio.ByteBuffer

class SoftVideoEncoderV2Impl(var context: CodecContext,
                             private val textureId: IntArray,
                             private var eglContext: EGLContext,
                             var codecWrapper: CodecTextureWrapper? = null,
                             private var imageReader: ImageReader? = null,
                             var codec: CacheX264Encoder? = null,
                             private var pTimer: PresentationTimer = PresentationTimer(context.video.fps),
                             override var onPreparedListener: Encoder.OnPreparedListener? = null,
                             override var onRecordListener: Encoder.OnRecordListener? = null)
    : Encoder, CacheX264Encoder.OnSampleListener, ImageReader.OnImageAvailableListener {

    override fun onFormatChanged(format: MediaFormat) {
        onSampleListener?.onFormatChanged(this, format)
    }

    override fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer) {
        pTimer.record()
        info.presentationTimeUs = pTimer.presentationTimeUs
        onSampleListener?.onSample(this, info, data)
        onRecordListener?.onRecord(this, info.presentationTimeUs)
    }

    private lateinit var format: MediaFormat
    private var mPipeline = EventPipeline.create("VideoEncodePipeline")
    private val mEncodingSyn = Any()
    private var mEncoding = false
    private var inited = false

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        initImageReader()
        mPipeline.queueEvent(Runnable {
            initCodec()
            pTimer.reset()
            inited = true
        })
    }

    private fun initImageReader() {
        imageReader = ImageReader.newInstance(context.video.width, context.video.height,
                PixelFormat.RGBA_8888, 5)
        imageReader?.setOnImageAvailableListener(this, mPipeline.getHandler())
        codecWrapper = CodecTextureWrapper(imageReader!!.surface, textureId, eglContext)
    }

    private fun initCodec() {
        format = CodecHelper.createVideoFormat(context, true)!!
        codec = CacheX264Encoder(X264Encoder(format))
        codec!!.setProfile("high")
        codec!!.setLevel(31)
        onPreparedListener?.onPrepared(this)
        codec?.onSampleListener = this
    }

    private fun encode(buffer: ByteBuffer, rowPadding: Int) {
        synchronized(mEncodingSyn) {
            if (!mEncoding) return
            codec?.encode(buffer, rowPadding)
        }
    }

    override fun start() {
        synchronized(mEncodingSyn) {
            pTimer.start()
            mEncoding = true
        }
    }

    override fun pause() {
        synchronized(mEncodingSyn) {
            mEncoding = false
        }
    }

    override fun stop() {
        pause()
        debug_e("Video encoder stopping")
        codec?.release()
        imageReader?.close()
        mPipeline.quit()
        codecWrapper?.release()
        codecWrapper = null
        debug_e("Video encoder stop")
    }

    private var start = 0L
    override fun onImageAvailable(reader: ImageReader) {
        start = System.currentTimeMillis()
        val image = reader.acquireNextImage()
        val planes = image.planes
        val width = image.width
        val rowStride = planes[0].rowStride
        val pixelStride = planes[0].pixelStride
        val rowPadding = rowStride - pixelStride * width
        encode(planes[0].buffer, rowPadding)
        image?.close()
//        debug_e("Encode cost ${System.currentTimeMillis() - start}")
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(mEncodingSyn) {
            if (mEncoding && inited) {
                codecWrapper?.egl?.makeCurrent()
                GLES20.glViewport(0, 0, context.video.width, context.video.height)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
                codecWrapper?.drawTexture(null)
//                codecWrapper?.egl?.setPresentationTime(System.nanoTime())
                codecWrapper?.egl?.swapBuffers()
            }
        }
    }
}