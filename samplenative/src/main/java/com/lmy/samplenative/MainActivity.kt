package com.lmy.samplenative

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import com.lmy.samplenative.processor.PictureProcessor
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(), TextureView.SurfaceTextureListener {
    private var processor: PictureProcessor? = PictureProcessor()

    override fun getLayoutResource(): Int = R.layout.activity_main
    override fun initView() {
        addBtn.setOnClickListener {
            addMessage()
        }
        textureView.surfaceTextureListener = this
    }

    external fun addMessage()
    external fun stop()

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        processor?.prepare(Surface(surface))
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    companion object {
        init {
            System.loadLibrary("hwvcom")
            System.loadLibrary("hwvc_render")
            System.loadLibrary("hwvc_test")
        }
    }
}
