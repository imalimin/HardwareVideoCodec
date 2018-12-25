package com.lmy.samplenative

import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.os.Environment
import android.view.Surface
import android.view.TextureView
import com.lmy.samplenative.processor.PictureProcessor
import kotlinx.android.synthetic.main.activity_main.*
import android.R.attr.bitmap
import android.util.Log
import java.nio.ByteBuffer


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
        processor?.prepare(Surface(surface), width, height)
        val bitmap = BitmapFactory.decodeFile("${Environment.getExternalStorageDirectory().path}/1.jpg")
        val bytes = bitmap.byteCount
        Log.e("11111", "size = $bytes")
        val buffer = ByteBuffer.allocate(bytes)
        bitmap.copyPixelsToBuffer(buffer)
        processor?.show(buffer.array(), bitmap.width, bitmap.height)
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
