/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.impl

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.EGLContext
import android.opengl.GLES20
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.loge
import com.lmy.codec.pipeline.EventPipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_v
import com.lmy.codec.wrapper.CodecTextureWrapper


/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class VideoEncoderImpl(var parameter: Parameter,
                       private var textureId: Int,
                       private var eglContext: EGLContext,
                       var codecWrapper: CodecTextureWrapper? = null,
                       private var codec: MediaCodec? = null,
                       private var mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo(),
                       private var pTimer: PresentationTimer = PresentationTimer(parameter.video.fps))
    : Encoder {

    companion object {
        private val WAIT_TIME = 10000L
    }

    private lateinit var format: MediaFormat
    private var mPipeline = EventPipeline.create("VideoEncodePipeline")
    private val mEncodingSyn = Any()
    private var mEncoding = false

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        initCodec()
        mPipeline.queueEvent(Runnable { init() })
    }

    private fun initCodec() {
        val f = CodecHelper.createVideoFormat(parameter)
        if (null == f) {
            loge("Unsupport codec type")
            return
        }
        format = f!!
        debug_v("create codec: ${format.getString(MediaFormat.KEY_MIME)}")
        try {
            codec = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME))
        } catch (e: Exception) {
            debug_e("Can not create codec")
        } finally {
            if (null == codec)
                debug_e("Can not create codec")
        }
    }

    private fun init() {
        if (null == codec) {
            debug_e("codec is null")
            return
        }
        pTimer.reset()
        codec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codecWrapper = CodecTextureWrapper(codec!!.createInputSurface(), textureId, eglContext)
        codecWrapper?.egl?.makeCurrent()
        codec!!.start()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        if (!mEncoding) return
        mPipeline.queueEvent(Runnable { encode() })
    }

    private fun encode() {
        synchronized(mEncodingSyn) {
            pTimer.record()
            codecWrapper?.egl?.makeCurrent()
            GLES20.glViewport(0, 0, parameter.video.width, parameter.video.height)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
            codecWrapper?.drawTexture(null)
            codecWrapper?.egl?.swapBuffers()
            dequeue()
        }
    }

    /**
     * 通过OpenGL来控制数据输入，省去了直接控制输入缓冲区的步骤，所以这里直接操控输出缓冲区即可
     */
    @SuppressLint("WrongConstant")
    private fun dequeue(): Boolean {
        try {
            /**
             * 从输出缓冲区取出一个Buffer，返回一个状态
             * 这是一个同步操作，所以我们需要给定最大等待时间WAIT_TIME，一般设置为10000ms
             */
            val flag = codec!!.dequeueOutputBuffer(mBufferInfo, WAIT_TIME)
            when (flag) {
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {//输出缓冲区改变，通常忽略
                    debug_v("INFO_OUTPUT_BUFFERS_CHANGED")
                }
                MediaCodec.INFO_TRY_AGAIN_LATER -> {//等待超时，需要再次等待，通常忽略
//                    debug_v("INFO_TRY_AGAIN_LATER")
                    return false
                }
            /**
             * 输出格式改变，很重要
             * 这里必须把outputFormat设置给MediaMuxer，而不能不能用inputFormat代替，它们时不一样的，不然无法正确生成mp4文件
             */
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    debug_v("INFO_OUTPUT_FORMAT_CHANGED")
                    onSampleListener?.onFormatChanged(this, codec!!.outputFormat)
                }
                else -> {
                    if (flag < 0) return@dequeue false//如果小于零，则跳过
                    val data = codec!!.outputBuffers[flag]//否则代表编码成功，可以从输出缓冲区队列取出数据
                    if (null != data) {
                        val endOfStream = mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        if (endOfStream == 0) {//如果没有收到BUFFER_FLAG_END_OF_STREAM信号，则代表输出数据时有效的
                            mBufferInfo.presentationTimeUs = pTimer.presentationTimeUs
                            onSampleListener?.onSample(this, mBufferInfo, data)
                        }
                        //缓冲区使用完后必须把它还给MediaCodec，以便再次使用，至此一个流程结束，再次循环
                        codec!!.releaseOutputBuffer(flag, false)
//                        if (endOfStream == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
//                            return true
//                        }
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
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
        while (dequeue()) {//取出编码器中剩余的帧
        }
        debug_e("Video encoder stop")
        //编码结束，发送结束信号，让surface不在提供数据
        codec!!.signalEndOfInputStream()
        codec!!.stop()
        codec!!.release()
        codecWrapper?.release()
        mPipeline.quit()
    }

    class PresentationTimer(var fps: Int,
                            var presentationTimeUs: Long = 0,
                            private var timestamp: Long = 0) {

        fun start() {
            timestamp = 0
        }

        fun record() {
            val timeTmp = System.nanoTime()
            presentationTimeUs += if (0L != timestamp)
                (timeTmp - timestamp) / 1000
            else
                1000000L / fps
            timestamp = timeTmp
        }

        fun reset() {
            presentationTimeUs = 0
            timestamp = 0
        }
    }
}