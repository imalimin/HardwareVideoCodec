package com.lmy.codec.x264

import android.media.MediaCodec
import android.media.MediaFormat
import com.lmy.codec.entity.RecycleQueue
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/6/7.
 */
class CacheX264Encoder(frameSize: Int,
                       private val codec: X264Encoder,
                       private var cache: Cache? = null,
                       var onSampleListener: OnSampleListener? = null,
                       private var running: Boolean = true) : X264, Runnable {

    private var mEncodeThread = Thread(this).apply { name = "CacheX264Encoder" }

    init {
        cache = Cache(frameSize, 5)
        codec.start()
        mEncodeThread.start()
    }

    fun encode(buffer: ByteBuffer) {
        val data = cache?.pollCache() ?: return
        buffer.position(0)
        buffer.get(data)
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