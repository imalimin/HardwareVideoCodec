package com.lmy.samplenative

import android.content.Intent
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.widget.SeekBar
import android.widget.Toast
import com.lmy.hwvcnative.processor.VideoProcessor
import kotlinx.android.synthetic.main.activity_video.*
import java.io.File

class VideoActivity : BaseActivity(), TextureView.SurfaceTextureListener,
        SeekBar.OnSeekBarChangeListener {

    private lateinit var mFilterController: FilterController
    private var processor: VideoProcessor? = VideoProcessor()
    private var surface: Surface? = null
    private var playing: Boolean = true

    override fun getLayoutResource(): Int = R.layout.activity_video
    override fun initView() {
        var uri = intent.data
        if (uri == null)
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
        if (uri == null) {
            val testFile = File(Environment.getExternalStorageDirectory(), "001.mp4")
            if (!testFile.exists()) {
                Toast.makeText(this, "没有找到该文件", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            uri= Uri.fromFile(testFile)
        }
        val path = getRealFilePath(uri)
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "没有找到该文件", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        mFilterController = FilterController(processor!!, progressLayout)
        filterBtn.setOnClickListener {
            mFilterController.chooseFilter(this)
        }
        seekBar.setOnSeekBarChangeListener(this)
        playBtn.setOnClickListener {
            if (playing) {
                processor?.pause()
            } else {
                processor?.start()
            }
            playing = !playing
        }
        processor?.setSource(path!!)
        surfaceView.keepScreenOn = true
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                processor?.release()
                processor = null
                Log.i("HWVC", "surfaceDestroyed")
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                processor?.prepare(holder.surface, surfaceView.width, surfaceView.height)
                processor?.start()
            }
        })
//        surfaceView.surfaceTextureListener = this
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
//                processor?.release()
//                processor = null
        this.surface?.release()
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        this.surface = Surface(surface)
        processor?.prepare(this.surface!!, width, height)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, p2: Boolean) {
        processor?.seek(progress.toLong())
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        processor?.pause()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        processor?.start()
    }
}