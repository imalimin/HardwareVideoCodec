package com.lmy.samplenative

import com.lmy.hwvcnative.media.Echoer
import kotlinx.android.synthetic.main.activity_echo.*

class EchoActivity : BaseActivity() {
    private var echo: Echoer? = null
    override fun getLayoutResource(): Int = R.layout.activity_echo
    override fun initView() {
        echo = Echoer()
        startBtn.setOnClickListener { echo?.start() }
        stopBtn.setOnClickListener { echo?.stop() }
    }

    override fun onDestroy() {
        super.onDestroy()
        echo?.stop()
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