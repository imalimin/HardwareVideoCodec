package com.lmy.sample

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.lmy.codec.presenter.VideoPlayer
import com.lmy.codec.presenter.VideoProcessor
import com.lmy.codec.presenter.impl.VideoPlayerImpl
import com.lmy.codec.presenter.impl.VideoProcessorImpl
import kotlinx.android.synthetic.main.activity_video.*
import java.io.File

class VideoActivity : BaseActivity(), VideoPlayer.OnPlayStateListener, View.OnTouchListener {

    private var player: VideoPlayer? = null
    private var processor: VideoProcessor? = null
    private var mFilterController: FilterController? = null
    private var dialog: AlertDialog? = null
    private var durationUs = 0L
    private var requestSeek = false
    override fun getLayoutResource(): Int = R.layout.activity_video
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
        val path = getRealFilePath(this, uri)
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "没有找到该文件", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val mTextureView = TextureView(this).apply {
            fitsSystemWindows = true
            keepScreenOn = true
        }
        mTextureContainer.addView(mTextureView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))
        player = VideoPlayerImpl(applicationContext).apply {
            reset()
            onPlayStateListener = this@VideoActivity
            setInputResource(path!!)
            setPreviewDisplay(mTextureView)
            prepare()
        }
        player?.start()
        mFilterController = FilterController(player!!, progressLayout)
        mTextureContainer.setOnClickListener {
            if (player!!.isPlaying())
                player?.pause()
            else
                player?.start()
        }
        initProcessor(path!!)
        mNowTimeView.text = getTimeString(0)
        mEndTimeView.text = getTimeString(0)
        mProgressBar.isEnabled = false
        mProgressBar.setOnTouchListener(this)
    }

    private fun initProcessor(path: String) {
        processor = VideoProcessorImpl.create(applicationContext)
        effectBtn.setOnClickListener {
            mFilterController?.chooseFilter(this)
        }
        saveBtn.setOnClickListener {
            dialog = AlertDialog.Builder(this@VideoActivity)
                    .setMessage("Waiting...")
                    .setCancelable(false)
                    .create()
            dialog?.show()
            processor?.reset()
            processor?.setInputResource(File(path))
            processor?.setFilter(player!!.getFilter()!!::class.java.newInstance())
            processor?.prepare()
            Toast.makeText(this, "Rendering", Toast.LENGTH_SHORT).show()
            val outputPath = getOutputPath(path)
            processor?.save(outputPath, Runnable {
                runOnUiThread {
                    dialog?.dismiss()
                    Toast.makeText(this, "Saved to $outputPath", Toast.LENGTH_SHORT).show()
                    insert(outputPath)
                }
            })
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (v.id) {
            mProgressBar.id ->
                if (MotionEvent.ACTION_UP == event.action) {
                    player?.seekTo((mProgressBar.progress * 1f / mProgressBar.max * durationUs).toLong())
                    synchronized(this) {
                        requestSeek = false
                    }
                } else if (MotionEvent.ACTION_DOWN == event.action) {
                    synchronized(this) {
                        requestSeek = true
                    }
                } else if (MotionEvent.ACTION_MOVE == event.action) {
                    runOnUiThread {
                        mNowTimeView.text = getTimeString((mProgressBar.progress * 1f
                                / mProgressBar.max * durationUs).toLong())
                    }
                }
        }
        return false
    }

    private fun insert(path: String) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(File(path))
        sendBroadcast(intent)
    }

    override fun onResume() {
        super.onResume()
        player?.start()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        processor?.release()
    }

    private fun getRealFilePath(context: Context, uri: Uri?): String? {
        if (null == uri) return null
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null)
            data = uri.path
        else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }

    private fun getOutputPath(input: String): String {
        return "${input.substring(0, input.lastIndexOf("."))}_filter.mp4"
    }

    private fun getTimeString(timeUs: Long): String {
        if (timeUs <= 0) return "00:00"
        val time = timeUs / 1000000
        val s = addZero((time % 60).toString())
        val m = addZero((time / 60).toString())
        return "$m:$s"
    }

    private fun addZero(str: String): String {
        if (str.isEmpty()) return "00"
        if (1 == str.length) return "0$str"
        return str;
    }

    override fun onPrepared(play: VideoPlayer, durationUs: Long) {
        mProgressBar.isEnabled = true
        this.durationUs = durationUs
        runOnUiThread { mEndTimeView.text = getTimeString(durationUs) }
        mProgressBar.progress = 0
    }

    override fun onStart(play: VideoPlayer) {
    }

    override fun onPause(play: VideoPlayer) {
    }

    override fun onStop(play: VideoPlayer) {
    }

    override fun onPlaying(play: VideoPlayer, timeUs: Long, durationUs: Long) {
        synchronized(this) {
            if (!requestSeek) {
                val progress = (timeUs * 1000f / durationUs).toInt()
                mProgressBar.progress = if (progress > mProgressBar.max) mProgressBar.max else progress
                runOnUiThread { mNowTimeView.text = getTimeString(timeUs) }
            }
        }
    }
}