package com.lmy.samplenative

import com.lmy.hwvcnative.media.AudioPlayerTest

class AudioPlayerActivity : BaseActivity() {
    private var playerTest = AudioPlayerTest()

    override fun getLayoutResource(): Int = R.layout.activity_audio_player

    override fun initView() {
        playerTest.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerTest.stop()
    }
}