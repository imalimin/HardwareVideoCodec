package com.lmy.samplenative

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import com.lmy.hwvcnative.processor.AudioProcessor
import java.io.File

class AudioActivity : BaseActivity() {
    private var processor: AudioProcessor? = AudioProcessor()
    override fun getLayoutResource(): Int = R.layout.activity_audio
    override fun initView() {
        var uri = intent.data
        if (uri == null)
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
        if (uri == null) {
            val testFile = File(Environment.getExternalStorageDirectory(), "001.mp4")
            if (!testFile.exists()) {
                Toast.makeText(this, "没有找到该文件", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            uri= Uri.fromFile(testFile)
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