package com.lmy.samplenative

import android.graphics.SurfaceTexture
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import com.lmy.hwvcnative.processor.PictureProcessor
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(), TextureView.SurfaceTextureListener {
    private lateinit var mFilterController: FilterController
    private var processor: PictureProcessor? = PictureProcessor()
    private var surface: Surface? = null

    override fun getLayoutResource(): Int = R.layout.activity_main
    override fun initView() {
        mFilterController = FilterController(processor!!, progressLayout)
        filterBtn.setOnClickListener {
            mFilterController.chooseFilter(this)
        }
        surfaceView.keepScreenOn = true
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                processor?.release()
                processor = null
                Log.i("00000", "surfaceDestroyed")
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
                processor?.prepare(holder.surface, surfaceView.width, surfaceView.height)
                processor?.show("${Environment.getExternalStorageDirectory().path}/1.jpg")
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
        processor?.show("${Environment.getExternalStorageDirectory().path}/1.jpg")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        init {
            System.loadLibrary("hwvcom")
            System.loadLibrary("hwvc_codec")
            System.loadLibrary("hwvc_render")
            System.loadLibrary("hwvc_native")
        }
    }
}
