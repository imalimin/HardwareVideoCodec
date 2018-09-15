package com.lmy.codec.x264

import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.MediaCodec
import android.view.Surface
import com.lmy.codec.entity.RecycleQueue
import com.lmy.codec.helper.GLHelper
import com.lmy.codec.helper.Libyuv
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.pipeline.impl.EventPipeline
import com.lmy.codec.util.debug_e
import java.nio.ByteBuffer

class SurfaceX264Encoder(private val codec: CacheX264Encoder,
                         var onSampleListener: CacheX264Encoder.OnSampleListener,
                         private val maxCache: Int = 5,
                         private var cache: Cache? = null,
                         private var running: Boolean = true,
                         private var imageReader: ImageReader? = null,
                         private var mPipeline: Pipeline = EventPipeline.create("SurfaceX264Encoder"),
                         private var width: Int = 0,
                         private var height: Int = 0
) : X264, Runnable, ImageReader.OnImageAvailableListener {

    private var mEncodeThread = Thread(this).apply { name = "SurfaceX264Encoder1" }

    var surface: Surface
    private val data: ByteArray
    private lateinit var yuv: ByteArray

    init {
        data = ByteArray(codec.getWidth() * codec.getHeight() * 4)
        codec.onSampleListener = onSampleListener
        imageReader = ImageReader.newInstance(getWidth(), getHeight(),
                PixelFormat.RGBA_8888, 5)
        imageReader?.setOnImageAvailableListener(this, mPipeline.getHandler())
        surface = imageReader!!.surface
        if (!asyn()) {
            yuv = ByteArray(codec.getWidth() * codec.getHeight() * 3 / 2)
        } else {
            cache = Cache(codec.getWidth() * codec.getHeight() * 3 / 2, maxCache)
            mEncodeThread.start()
        }
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

    override fun encode(src: ByteArray): MediaCodec.BufferInfo? {
        return null
    }

    override fun getWidth(): Int {
        if (width <= 0) width = codec.getWidth()
        return width
    }

    override fun getHeight(): Int {
        if (height <= 0) height = codec.getHeight()
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
            codec.release()
            cache?.release()
        }
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