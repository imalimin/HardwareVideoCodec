package com.lmy.samplenative

import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import com.lmy.samplenative.processor.PictureProcessor


class MainActivity : BaseActivity() {
    private lateinit var addBtn: Button
    private lateinit var surfaceView: SurfaceView
    private var processor: PictureProcessor? = PictureProcessor()

    override fun getLayoutResource(): Int = R.layout.activity_main
    override fun initView() {
        addBtn = findViewById(R.id.addBtn)
        surfaceView = findViewById(R.id.surfaceView)
        addBtn.setOnClickListener {
            addMessage()
        }
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                processor?.prepare(holder.surface)
            }
        })
    }

    external fun addMessage()
    external fun stop()
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
