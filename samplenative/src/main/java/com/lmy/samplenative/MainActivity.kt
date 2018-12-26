package com.lmy.samplenative

import android.graphics.SurfaceTexture
import android.os.Environment
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import com.lmy.hwvc_native.processor.PictureProcessor
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(), TextureView.SurfaceTextureListener {
    private var processor: PictureProcessor? = PictureProcessor()

    override fun getLayoutResource(): Int = R.layout.activity_main
    override fun initView() {
        addBtn.setOnClickListener {
        }
        surfaceView.keepScreenOn = true
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                processor?.prepare(holder.surface, surfaceView.width, surfaceView.height)
                processor?.show("${Environment.getExternalStorageDirectory().path}/1.jpg")
            }
        })
//        textureView.surfaceTextureListener = this
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        processor?.prepare(Surface(surface), width, height)
        processor?.show("${Environment.getExternalStorageDirectory().path}/1.jpg")
    }

    override fun onDestroy() {
        super.onDestroy()
        processor?.release()
        processor = null
    }

    companion object {
        init {
            System.loadLibrary("hwvcom")
            System.loadLibrary("hwvc_render")
            System.loadLibrary("hwvc_native")
        }
    }
}
