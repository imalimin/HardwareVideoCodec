package com.lmy.codec.impl

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import com.lmy.codec.Encoder
import com.lmy.codec.Muxer
import com.lmy.codec.entity.Parameter
import com.lmy.codec.entity.Sample
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRenderImpl
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.CameraWrapper
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraPreviewPresenter(var parameter: Parameter,
                             var encoder: Encoder? = null,
                             var audioEncoder: Encoder? = null,
                             private var cameraWrapper: CameraWrapper? = null,
                             private var render: Render? = null,
                             private var muxer: Muxer? = MuxerImpl("/storage/emulated/0/test.mp4"))
    : SurfaceTexture.OnFrameAvailableListener, Encoder.OnSampleListener {

    private val syncOp = Any()
    private var onStateListener: OnStateListener? = null
    private val onAudioSampleListener: Encoder.OnSampleListener = object : Encoder.OnSampleListener {
        override fun onFormatChanged(format: MediaFormat) {
            debug_e("Add audio track")
            muxer?.addAudioTrack(format)
        }

        override fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer) {
//            debug_e("audio sample(${info.size})")
            muxer?.writeAudioSample(Sample.wrap(info, data))
        }
    }

    init {
        cameraWrapper = CameraWrapper.open(parameter, this)
        render = DefaultRenderImpl(parameter, cameraWrapper!!.textureWrapper as CameraTextureWrapper)
    }

    override fun onFormatChanged(format: MediaFormat) {
        debug_e("Format(mime=${format.getString(MediaFormat.KEY_MIME)}," +
                "width=${format.getInteger(MediaFormat.KEY_WIDTH)}," +
                "height=${format.getInteger(MediaFormat.KEY_HEIGHT)}," +
                "bitrate=${format.getInteger(MediaFormat.KEY_BIT_RATE)}," +
//                "fps=${format.getInteger(MediaFormat.KEY_FRAME_RATE)}," +
//                "iFrame=${format.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL)}," +
//                "captureRate=${format.getInteger(MediaFormat.KEY_CAPTURE_RATE)}," +
//                "bitrateMode=${format.getInteger(MediaFormat.KEY_BITRATE_MODE)}," +
                "colorRange=${format.getInteger(MediaFormat.KEY_COLOR_RANGE)}," +
                "colorStandard=${format.getInteger(MediaFormat.KEY_COLOR_STANDARD)}," +
                "colorTransfer=${format.getInteger(MediaFormat.KEY_COLOR_TRANSFER)}," +
//                "complexity=${format.getInteger(MediaFormat.KEY_COMPLEXITY)}," +
//                "duration=${format.getInteger(MediaFormat.KEY_DURATION)}," +
//                "hdrInfo=${format.getString(MediaFormat.KEY_HDR_STATIC_INFO)}," +
//                "period=${format.getInteger(MediaFormat.KEY_INTRA_REFRESH_PERIOD)}," +
//                "isDTS=${format.getInteger(MediaFormat.KEY_IS_ADTS)}," +
//                "isAutoSelect=${format.getInteger(MediaFormat.KEY_IS_AUTOSELECT)}," +
//                "isDefault=${format.getInteger(MediaFormat.KEY_IS_DEFAULT)}," +
//                "isSubtitle=${format.getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE)}," +
//                "language=${format.getString(MediaFormat.KEY_LANGUAGE)}," +
//                "latency=${format.getInteger(MediaFormat.KEY_LATENCY)}," +
//                "level=${format.getInteger(MediaFormat.KEY_LEVEL)}," +
//                "maxWidth=${format.getInteger(MediaFormat.KEY_MAX_WIDTH)}," +
//                "maxHeight=${format.getInteger(MediaFormat.KEY_MAX_HEIGHT)}," +
//                "maxInputSize=${format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)}," +
//                "operatingRate=${format.getInteger(MediaFormat.KEY_OPERATING_RATE)}," +
//                "priority=${format.getInteger(MediaFormat.KEY_PRIORITY)}," +
//                "profile=${format.getInteger(MediaFormat.KEY_PROFILE)}," +
//                "onStop=${format.getInteger(MediaFormat.KEY_PUSH_BLANK_BUFFERS_ON_STOP)}," +
//                "frameAfter=${format.getInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER)}," +
//                "rotation=${format.getInteger(MediaFormat.KEY_ROTATION)}," +
//                "sampleRate=${format.getInteger(MediaFormat.KEY_SAMPLE_RATE)}," +
//                "sliceHeight=${format.getInteger(MediaFormat.KEY_SLICE_HEIGHT)}," +
//                "stride=${format.getInteger(MediaFormat.KEY_STRIDE)}," +
//                "layering=${format.getInteger(MediaFormat.KEY_TEMPORAL_LAYERING)}," +
//                "trackId=${format.getInteger(MediaFormat.KEY_TRACK_ID)}," +
//                "color=${format.getInteger(MediaFormat.KEY_COLOR_FORMAT)}" +
                ")")
        muxer?.addVideoTrack(format)
    }

    /**
     * 编码后的帧数据
     * For VideoEncoderImpl
     */
    override fun onSample(info: MediaCodec.BufferInfo, data: ByteBuffer) {
        debug_e("BufferInfo(size=${info.size}, " +
                "timestamp=${info.presentationTimeUs}," +
                "offset=${info.offset}," +
                "flags=${info.flags})")
        if (info.flags == 2) {
            var msg = ""
            for (i in 0 until info.size) {
                msg += "${data[i]}, "
            }
            debug_e(msg)
        }
        muxer?.writeVideoSample(Sample.wrap(info, data))
    }

    /**
     * Camera有数据生成时回调
     * For CameraWrapper
     */
    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
        render?.onFrameAvailable()?.afterRender(Runnable {
            encoder?.onFrameAvailable(cameraTexture)
        })
    }

    fun startPreview(screenTexture: SurfaceTexture, width: Int, height: Int) {
        synchronized(syncOp) {
            cameraWrapper!!.startPreview()
            render?.start(screenTexture, width, height, Runnable {
                encoder = SoftVideoEncoderImpl(parameter,
                        cameraWrapper!!.textureWrapper as CameraTextureWrapper)
                encoder!!.setOnSampleListener(this@CameraPreviewPresenter)
                audioEncoder = AudioEncoderImpl(parameter)
                audioEncoder!!.setOnSampleListener(onAudioSampleListener)
            })
        }
    }

    fun updatePreview(width: Int, height: Int) {
//        mRender?.updatePreview(width, height)
    }

    fun stopPreview() {
        synchronized(syncOp) {
            release()
        }
    }

    private fun release() {
        synchronized(syncOp) {
            try {
                render?.stop()
                render?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                cameraWrapper?.release()
                cameraWrapper = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        stopEncoder()
    }

    private fun stopEncoder() {
        encoder?.stop(object : Encoder.OnStopListener {
            override fun onStop() {
                audioEncoder?.stop(object : Encoder.OnStopListener {
                    override fun onStop() {
                        muxer?.release()
                        onStateListener?.onStop()
                    }
                })
            }
        })
    }

    fun setOnStateListener(listener: OnStateListener) {
        onStateListener = listener
    }

    interface OnStateListener {
        fun onStop()
    }
}