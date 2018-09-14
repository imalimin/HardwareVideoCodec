package com.lmy.codec.x264

import android.media.MediaCodec
import android.media.MediaFormat
import com.lmy.codec.entity.Egl
import com.lmy.codec.entity.RecycleQueue
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/6/7.
 */
class CacheX264Encoder(private val codec: X264Encoder,
                       private var cache: Cache? = null,
                       var onSampleListener: OnSampleListener? = null,
                       private var running: Boolean = true) : X264, Runnable {

    private var mEncodeThread = Thread(this).apply { name = "CacheX264Encoder" }

    init {
        cache = Cache(codec.getWidth() * codec.getHeight() * Egl.COLOR_CHANNELS, 5)
        codec.start()
        mEncodeThread.start()
    }

    fun encode(buffer: ByteBuffer) {
        encode(buffer, 0)
    }

    private var offset = 0
    fun encode(buffer: ByteBuffer, rowPadding: Int) {
        val data = cache?.pollCache() ?: return
        buffer.rewind()
        if (rowPadding <= 0) {
            buffer.get(data)
        } else {
            offset = 0
            val width = getWidth()
            val height = getHeight()
            for (i in 0 until height) {
                buffer.get(data, offset, width * 4)
                offset += width * 4
                buffer.position(offset + i * rowPadding)
            }
        }
        cache?.offer(data)
    }

    override fun encode(src: ByteArray): MediaCodec.BufferInfo? {
        return codec.encode(src)
    }

    override fun stop() {
        if (!running) return
        running = false
        mEncodeThread.interrupt()
    }

    override fun release() {
        stop()
    }

    override fun run() {
        while (running) {
            var data: ByteArray?
            try {
                data = cache!!.take()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                break
            }
            val bufferInfo = encode(data) ?: continue
            cache!!.recycle(data)
            if (X264Encoder.BUFFER_FLAG_CODEC_CONFIG == bufferInfo.flags) {
                onSampleListener?.onFormatChanged(codec.getOutFormat())
            } else {
                onSampleListener?.onSample(bufferInfo, codec.getOutBuffer())
            }
        }
        codec.release()
        cache?.release()
    }

    override fun getWidth(): Int = codec.getWidth()

    override fun getHeight(): Int = codec.getHeight()
    override fun setLevel(level: Int) {
        codec.setLevel(level)
    }

    override fun setProfile(profile: String) {
        codec.setProfile(profile)
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

    interface OnSampleListener {
        fun onFormatChanged(format: MediaFormat)
        fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer)
    }
}