package com.lmy.samplenative

import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import com.lmy.hwvcnative.processor.AudioProcessor

class AudioActivity : BaseActivity() {
    private var processor: AudioProcessor? = AudioProcessor()
    override fun getLayoutResource(): Int = R.layout.activity_audio
    override fun initView() {
        var uri = intent.data
        if (uri == null)
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
        if (uri == null) {
            finish()
            Toast.makeText(this, "没有找到该文件", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val path = getRealFilePath(uri)
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "没有找到该文件", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        processor?.setSource(path!!)
        processor?.prepare()
        processor?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        processor?.stop()
        processor?.release()
    }
}