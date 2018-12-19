package com.lmy.samplenative

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun getLayoutResource(): Int = R.layout.activity_main
    override fun initView() {
        addBtn.setOnClickListener {

        }
    }

    external fun addMessage();

    companion object {
        init {
            System.loadLibrary("lmy_render")
        }
    }
}
