package com.lmy.samplenative

import android.view.SurfaceView
import android.widget.Button


class MainActivity : BaseActivity() {
    private lateinit var addBtn: Button
    private lateinit var surfaceView: SurfaceView

    override fun getLayoutResource(): Int = R.layout.activity_main
    override fun initView() {
        addBtn = findViewById(R.id.addBtn)
        surfaceView = findViewById(R.id.surfaceView)
        addBtn.setOnClickListener {
            addMessage()
        }
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
