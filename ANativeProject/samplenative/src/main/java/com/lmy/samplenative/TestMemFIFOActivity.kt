package com.lmy.samplenative

import com.lmy.hwvcnative.test.MemFIFOTest
import kotlinx.android.synthetic.main.activity_test_mem_fifo.*

class TestMemFIFOActivity : BaseActivity() {
    private val tester = MemFIFOTest()
    private var count = 0
    override fun getLayoutResource(): Int = R.layout.activity_test_mem_fifo

    override fun initView() {
        textView.text
        linearLayout.setOnClickListener {
            tester.push()
            ++count
            if (count >= 8) {
                tester.take()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tester.release()
    }
}