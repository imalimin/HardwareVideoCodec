/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.sample

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioGroup
import com.lmy.codec.RecordPresenter
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.loge
import com.lmy.codec.presenter.impl.VideoRecorderImpl
import com.lmy.codec.texture.impl.filter.NormalFilter
import com.lmy.codec.util.debug_e
import com.lmy.sample.helper.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnTouchListener, RadioGroup.OnCheckedChangeListener {

    private lateinit var mRecorder: VideoRecorderImpl
    private lateinit var mFilterController: FilterController
    private var defaultVideoWidth = 0
    private var defaultVideoHeight = 0
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fillStatusBar()
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        ratioGroup.check(ratioGroup.getChildAt(0).id)
        ratioGroup.setOnCheckedChangeListener(this)
        loge("Permission: " + PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
        if (!PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
            return
        //Init TextureView
        val mTextureView = TextureView(this).apply {
            fitsSystemWindows = true
            keepScreenOn = true
            setOnTouchListener(this@MainActivity)
        }
        mTextureContainer.addView(mTextureView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))
        //Init VideoRecorderImpl
        mRecorder = VideoRecorderImpl(this).apply {
            reset()
            enableHardware(true)//Default true
            setOutputUri("${Environment.getExternalStorageDirectory().absolutePath}/test_${++count}.mp4")
//            setOutputUri("rtmp://192.168.16.125:1935/live/livestream")
            setOutputSize(720, 1280)//Default 720x1280
            setFilter(NormalFilter::class.java)//Default NormalFilter
            setPreviewDisplay(mTextureView)
            setOnStateListener(onStateListener)
        }
        mRecorder.prepare()
        mFilterController = FilterController(mRecorder, progressLayout)
        defaultVideoWidth = mRecorder.getWidth()
        defaultVideoHeight = mRecorder.getHeight()

        effectBtn.setOnClickListener({
            mFilterController.chooseFilter(this)
        })
        nextBtn.setOnClickListener {
            nextBtn.isEnabled = false
            mRecorder.stop()
            mRecorder.reset()
            mRecorder.setOutputUri("${Environment.getExternalStorageDirectory().absolutePath}/test_${++count}.mp4")
            mRecorder.prepare()
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!mRecorder.prepared()) return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mRecorder.start()
            }
            MotionEvent.ACTION_UP -> {
                mRecorder.pause()
            }
        }
        return true
    }

    private var onStateListener =
            object : RecordPresenter.OnStateListener {
                override fun onStop() {

                }

                override fun onPrepared(encoder: Encoder) {
                    nextBtn.isEnabled = true
                    runOnUiThread {
                        enableChangeRatio(true)
                        timeView.text = "00:00.00"
                    }
                }

                override fun onRecord(encoder: Encoder, timeUs: Long) {
                    runOnUiThread {
                        timeView.text = formatTimeUs(timeUs)
                    }
                }
            }

    private fun formatTimeUs(timeUs: Long): String {
        val second = timeUs / 1000000
        var ms = (timeUs / 10000 % 100).toString()
        ms = if (1 == ms.length) "0$ms" else ms
        var s = (second % 60).toString()
        s = if (1 == s.length) "0$s" else s
        var m = (second / 60).toString()
        m = if (1 == m.length) "0$m" else m
        return "$m:$s.$ms"
    }

    private fun enableChangeRatio(enable: Boolean) {
        for (i in 0 until ratioGroup.childCount) {
            ratioGroup.getChildAt(i).isEnabled = enable
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val width = mRecorder.getWidth()
        var height = when (group.indexOfChild(group.findViewById(checkedId))) {
            1 -> {//1:1
                width
            }
            2 -> {//4:3
                (width / 4f * 3).toInt()
            }
            3 -> {//3:2
                (width / 3f * 2).toInt()

            }
            else -> {//默认
                defaultVideoHeight
            }
        }
        if (0 != height % 2) {
            ++height
        }
        enableChangeRatio(false)
        nextBtn.isEnabled = false
        mRecorder.stop()
        mRecorder.reset()
        mRecorder.setOutputSize(width, height)
        mRecorder.setOutputUri("${Environment.getExternalStorageDirectory().absolutePath}/test_${++count}.mp4")
        mRecorder.prepare()
    }

    private fun showPermissionsDialog() {
        AlertDialog.Builder(this)
                .setMessage("Please grant permission in the permission settings")
                .setNegativeButton("cancel", { dialog, which -> finish() })
                .setPositiveButton("enter", { dialog, which ->
                    PermissionHelper.gotoPermissionManager(this@MainActivity)
                    finish()
                })
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (null == grantResults || grantResults.isEmpty()) return
        when (requestCode) {
            PermissionHelper.REQUEST_MY -> {
                if (PermissionHelper.checkGrantResults(grantResults)) {
                    initView()
                } else {
                    showPermissionsDialog()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRecorder.release()
        debug_e("onDestroy")
    }

    private fun fillStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }
}
