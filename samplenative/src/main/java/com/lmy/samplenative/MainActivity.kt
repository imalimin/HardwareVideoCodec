package com.lmy.samplenative

import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.os.Environment
import android.view.Surface
import android.view.TextureView
import kotlinx.android.synthetic.main.activity_main.*
import android.R.attr.bitmap
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.lmy.hwvc_native.processor.PictureProcessor
import java.nio.ByteBuffer


class MainActivity : BaseActivity(), TextureView.SurfaceTextureListener {
    private var processor: PictureProcessor? = PictureProcessor()

    override fun getLayoutResource(): Int = R.layout.activity_main
    override fun initView() {
        addBtn.setOnClickListener {
        }
        surfaceView.holder.addCallback(object:SurfaceHolder.Callback{
            override fun surfaceChanged(holder: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                processor?.prepare(holder.surface, surfaceView.width, surfaceView.height)
                val bitmap = BitmapFactory.decodeFile("${Environment.getExternalStorageDirectory().path}/1.jpg")
                val bytes = bitmap.byteCount
                val buffer = ByteBuffer.allocate(bytes)
                bitmap.copyPixelsToBuffer(buffer)
                val data = ByteArray(buffer.capacity())
                buffer.rewind()
                buffer.get(data)
                Log.e("11111", "size = ${data.size}")
                processor?.show(data, bitmap.width, bitmap.height)
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
        val bitmap = BitmapFactory.decodeFile("${Environment.getExternalStorageDirectory().path}/1.jpg")
        val bytes = bitmap.byteCount
        val buffer = ByteBuffer.allocate(bytes)
        bitmap.copyPixelsToBuffer(buffer)
        val data = ByteArray(buffer.capacity())
        buffer.rewind()
        buffer.get(data)
        Log.e("11111", "size = ${data.size}")
        processor?.show(data, bitmap.width, bitmap.height)
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
