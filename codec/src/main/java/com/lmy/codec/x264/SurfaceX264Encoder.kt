package com.lmy.codec.x264

import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import com.lmy.codec.entity.RecycleQueue
import com.lmy.codec.helper.GLHelper
import com.lmy.codec.helper.Libyuv
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.util.debug_e
import java.nio.ByteBuffer

class SurfaceX264Encoder(private var format: MediaFormat,
                         var onSampleListener: CacheX264Encoder.OnSampleListener,
                         private val maxCache: Int = 5,
                         private var cache: Cache? = null,
                         private var running: Boolean = true,
                         private var imageReader: ImageReader? = null,
                         private var mPipeline: Pipeline = EventPipeline.create("SfX264Encoder"),
                         private var width: Int = 0,
                         private var height: Int = 0
) : X264, Runnable, ImageReader.OnImageAvailableListener {

    private var mEncodeThread = Thread(this).apply { name = "SfX264Encoder1" }

    private lateinit var codec: CacheX264Encoder
    var surface: Surface
    private val data: ByteArray
    private lateinit var yuv: ByteArray

    init {
        data = ByteArray(getWidth() * getHeight() * 4)
        imageReader = ImageReader.newInstance(getWidth(), getHeight(),
                PixelFormat.RGBA_8888, 5)
        imageReader?.setOnImageAvailableListener(this, mPipeline.getHandler())
        surface = imageReader!!.surface
        if (!asyn()) {
            yuv = ByteArray(getWidth() * getHeight() * 3 / 2)
        } else {
            cache = Cache(getWidth() * getHeight() * 3 / 2, maxCache)
            mEncodeThread.start()
        }
        mPipeline.queueEvent(Runnable {
            codec = CacheX264Encoder(X264Encoder(format, Libyuv.COLOR_I420))
            codec.onSampleListener = onSampleListener
        })
    }

    override fun start() {
        codec.start()
    }

    private fun asyn(): Boolean = maxCache > 0
    override fun run() {
        while (running) {
            var yuv: ByteArray?
            try {
                yuv = cache!!.take()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                break
            }
            codec.encode(yuv)
            cache!!.recycle(yuv)
        }
        codec.release()
        cache?.release()
    }

    fun encode(buffer: ByteBuffer, rowPadding: Int) {
        val yuv = if (asyn()) cache?.pollCache() ?: return else yuv
        GLHelper.copyToByteArray(buffer, data, getHeight(), getWidth() * 4, rowPadding)
        Libyuv.ConvertToI420(data, yuv, getWidth(), getHeight(), Libyuv.kRotate0)
        if (asyn()) {
            cache?.offer(yuv)
        } else {
            codec.encode(yuv)
        }
    }

    override fun onImageAvailable(reader: ImageReader) {
        val ttt = System.currentTimeMillis()
        val image = reader.acquireNextImage()
        val planes = image.planes
        val width = image.width
        val rowStride = planes[0].rowStride
        val pixelStride = planes[0].pixelStride
        val rowPadding = rowStride - pixelStride * width
        encode(planes[0].buffer, rowPadding)
        image?.close()
        debug_e("Encode cost ${System.currentTimeMillis() - ttt}")
    }

    override fun post(event: Runnable): SurfaceX264Encoder {
        mPipeline.queueEvent(event)
        return this
    }

    override fun encode(src: ByteArray): MediaCodec.BufferInfo? {
        return null
    }

    override fun getWidth(): Int {
        if (width <= 0) width = format.getInteger(MediaFormat.KEY_WIDTH)
        return width
    }

    override fun getHeight(): Int {
        if (height <= 0) height = format.getInteger(MediaFormat.KEY_HEIGHT)
        return height
    }

    override fun setProfile(profile: String) {
        codec.setProfile(profile)
    }

    override fun setLevel(level: Int) {
        codec.setLevel(level)
    }

    override fun stop() {
        if (!running) return
        running = false
        mEncodeThread.interrupt()
    }

    override fun release() {
        if (asyn()) {
            stop()
        } else {
            mPipeline.queueEvent(Runnable {
                codec.release()
                cache?.release()
            })
        }
        imageReader?.close()
        mPipeline.quit()
    }

    class Cache(private var size: Int,
                capacity: Int) : RecycleQueue<ByteArray>(capacity) {
        init {
            ready()
        }

        override fun newCacheEntry(): ByteArray {
            return ByteArray(size)
        }
    }
}