package com.lmy.samplenative.ui

import android.text.TextUtils
import com.lmy.hwvcnative.test.TestHwAudioRecorder
import com.lmy.samplenative.BaseActivity
import com.lmy.samplenative.R
import kotlinx.android.synthetic.main.activity_test_audio_recorder.*

class TestAudioRecorderActivity : BaseActivity() {
    private val recorder = TestHwAudioRecorder()
    override fun getLayoutResource() = R.layout.activity_test_audio_recorder

    override fun initView() {
        handleBtn.setOnClickListener {
            if (TextUtils.equals(handleBtn.text, "Record")) {
                handleBtn.text = "Play"
                recorder.start()
            } else if (TextUtils.equals(handleBtn.text, "Play")) {
                handleBtn.text = "Record"
                recorder.play()
            }
        }
    }
}