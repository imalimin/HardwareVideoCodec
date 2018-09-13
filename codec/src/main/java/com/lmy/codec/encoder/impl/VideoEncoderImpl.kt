/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.encoder.impl

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.EGLContext
import android.opengl.GLES20
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.PresentationTimer
import com.lmy.codec.helper.CodecHelper
import com.lmy.codec.loge
import com.lmy.codec.pipeline.EventPipeline
import com.lmy.codec.pipeline.GLEventPipeline
import com.lmy.codec.pipeline.Pipeline
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_v
import com.lmy.codec.wrapper.CodecTextureWrapper


/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class VideoEncoderImpl(var context: CodecContext,
                       private var textureId: IntArray,
                       private var eglContext: EGLContext,
                       private var asyn: Boolean = false,
                       var codecWrapper: CodecTextureWrapper? = null,
                       private var codec: MediaCodec? = null,
                       private var mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo(),
                       private var pTimer: PresentationTimer = PresentationTimer(context.video.fps),
                       override var onPreparedListener: Encoder.OnPreparedListener? = null,
                       override var onRecordListener: Encoder.OnRecordListener? = null)
    : Encoder {

    companion object {
        private val WAIT_TIME = 10000L
    }

    private lateinit var format: MediaFormat
    private var mPipeline: Pipeline = if (asyn) {
        EventPipeline.create("VideoEncodePipeline")
    } else {
        GLEventPipeline.INSTANCE
    }
    private val mEncodingSyn = Any()
    private var mEncoding = false
    private var mFrameCount = 0

    private var onSampleListener: Encoder.OnSampleListener? = null
    override fun setOnSampleListener(listener: Encoder.OnSampleListener) {
        onSampleListener = listener
    }

    init {
        initCodec()
        mPipeline.queueEvent(Runnable { init() })
    }

    private fun initCodec() {
        val f = CodecHelper.createVideoFormat(context)
        if (null == f) {
            loge("Unsupport codec type")
            return
        }
        format = f
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
        onPreparedListener?.onPrepared(this)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        if (!mEncoding) return
        mPipeline.queueEvent(Runnable { encode() })
    }

    private fun encode() {
        synchronized(mEncodingSyn) {
            pTimer.record()
            codecWrapper?.egl?.makeCurrent()
            GLES20.glViewport(0, 0, context.video.width, context.video.height)
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
                    val buffer = codec!!.outputBuffers[flag]//否则代表编码成功，可以从输出缓冲区队列取出数据
                    if (null != buffer) {
                        val endOfStream = mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        if (endOfStream == 0) {//如果没有收到BUFFER_FLAG_END_OF_STREAM信号，则代表输出数据时有效的
                            if (mBufferInfo.size != 0) {
                                ++mFrameCount
                                buffer.position(mBufferInfo.offset)
                                buffer.limit(mBufferInfo.offset + mBufferInfo.size)
                                mBufferInfo.presentationTimeUs = pTimer.presentationTimeUs
                                onSampleListener?.onSample(this, mBufferInfo, buffer)
                                onRecordListener?.onRecord(this, mBufferInfo.presentationTimeUs)
                            }
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
        debug_e("Video encoder stop")
        pause()
        if (mFrameCount > 0) {
            while (dequeue()) {//取出编码器中剩余的帧
            }
            //编码结束，发送结束信号，让surface不在提供数据
            codec!!.signalEndOfInputStream()
        }
        mFrameCount = 0
        codec!!.stop()
        codec!!.release()
        mPipeline.queueEvent(Runnable {
            codecWrapper?.release()
            codecWrapper = null
        })
        mPipeline.quit()
    }
}