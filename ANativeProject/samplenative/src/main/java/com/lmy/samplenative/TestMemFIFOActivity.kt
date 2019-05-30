package com.lmy.samplenative

import com.lmy.hwvcnative.test.MemFIFOTest
import kotlinx.android.synthetic.main.activity_test_mem_fifo.*

class TestMemFIFOActivity : BaseActivity() {
    private val tester = MemFIFOTest()
    private var count = 0
    private var thread1: Thread? = null
    private var thread2: Thread? = null
    override fun getLayoutResource(): Int = R.layout.activity_test_mem_fifo

    override fun initView() {
        textView.text
        linearLayout.setOnClickListener {
            thread1 = object : Thread() {
                override fun run() {
                    while (true) {
                        if (interrupted()) {
                            break
                        }
                        tester.push()
                        sleep(21)
                    }
                }
            }
            thread1?.start()
            thread2 = object : Thread() {
                override fun run() {
                    while (true) {
                        if (interrupted()) {
                            break
                        }
                        tester.take()
                        sleep(21)
                    }
                }
            }
            thread2?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        thread1?.interrupt()
        thread2?.interrupt()
        thread1?.join()
        thread2?.join()
        tester.release()
    }
}