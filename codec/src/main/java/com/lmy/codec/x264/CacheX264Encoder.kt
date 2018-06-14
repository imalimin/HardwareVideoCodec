package com.lmy.codec.x264

import android.media.MediaCodec
import com.lmy.codec.Encoder
import com.lmy.codec.entity.RecycleQueue
import com.lmy.codec.x265.X265Encoder
import java.nio.ByteBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/6/7.
 */
class CacheX264Encoder(frameSize: Int,
                       private val codec: X265Encoder,
                       private var cache: Cache? = null,
                       var onSampleListener: Encoder.OnSampleListener? = null) : X264, Runnable {

    private var mEncodeThread = Thread(this).apply { name = "CacheX264Encoder" }

    init {
        cache = Cache(frameSize, 5)
        codec.start()
        mEncodeThread.start()
    }

    fun encode(buffer: ByteBuffer) {
        var data = cache?.pollCache() ?: return
        buffer.position(0)
        if (buffer.hasArray()) {
            data = buffer.array()
        } else {
            buffer.get(data)
        }
        cache?.offer(data)
    }

    override fun encode(src: ByteArray): MediaCodec.BufferInfo? {
        return codec.encode(src)
    }

    override fun stop() {
        mEncodeThread.interrupt()
        codec.stop()
    }

    override fun release() {
        mEncodeThread.interrupt()
        codec.release()
        cache?.release()
    }

    override fun run() {
        while (true) {
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
}