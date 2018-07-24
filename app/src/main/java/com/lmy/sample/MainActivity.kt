/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.sample

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
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
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.loge
import com.lmy.codec.util.debug_e
import com.lmy.sample.helper.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener,
        View.OnTouchListener, RadioGroup.OnCheckedChangeListener {

    private lateinit var mPresenter: RecordPresenter
    private lateinit var mFilterController: FilterController
    private var defaultVideoWidth = 0
    private var defaultVideoHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        ratioGroup.check(ratioGroup.getChildAt(0).id)
        ratioGroup.setOnCheckedChangeListener(this)
        loge("Permission: " + PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
        if (!PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
            return
        val context = CodecContext(this)
        context.ioContext.path = "${Environment.getExternalStorageDirectory().absolutePath}/test.mp4"
        mPresenter = RecordPresenter(context)
        mPresenter.setOnStateListener(onStateListener)
        defaultVideoWidth = mPresenter.context.video.width
        defaultVideoHeight = mPresenter.context.video.height
        val mTextureView = TextureView(this)
        mTextureContainer.addView(mTextureView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))
        mTextureView.keepScreenOn = true
        mTextureView.surfaceTextureListener = this
        mTextureView.setOnTouchListener(this)
        mFilterController = FilterController(mPresenter, progressLayout)
        changeBtn.setOnClickListener({
            mFilterController.chooseFilter(this)
        })
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {
        mPresenter.updatePreview(p1, p2)
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
        mPresenter.stopPreview()
        return true
    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
        if (null != p0)
            mPresenter.startPreview(p0, p1, p2)
        debug_e("onSurfaceTextureAvailable")
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mPresenter.encoder?.start()
                mPresenter.audioEncoder?.start()
            }
            MotionEvent.ACTION_UP -> {
                mPresenter.encoder?.pause()
                mPresenter.audioEncoder?.pause()
            }
        }
        return true
    }

    private var onStateListener =
            object : RecordPresenter.OnStateListener {
                override fun onStop() {

                }

                override fun onPrepared(encoder: Encoder) {
                    runOnUiThread {
                        enableChangeRatio(true)
                        timeView.text = "00:00"
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
        var s = (second % 60).toString()
        s = if (1 == s.length) "0$s" else s
        var m = (second / 60).toString()
        m = if (1 == m.length) "0$m" else m
        return "$m:$s"
    }

    private fun enableChangeRatio(enable: Boolean) {
        for (i in 0 until ratioGroup.childCount) {
            ratioGroup.getChildAt(i).isEnabled = enable
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val width = mPresenter.context.video.width
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
        mPresenter.updateSize(width, height)
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
        debug_e("onDestroy")
    }
}
