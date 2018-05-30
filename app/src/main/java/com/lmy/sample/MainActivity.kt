/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.sample

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import com.lmy.codec.CameraPreviewPresenter
import com.lmy.codec.loge
import com.lmy.codec.texture.impl.filter.BeautyFilter
import com.lmy.codec.texture.impl.filter.GreyFilter
import com.lmy.codec.texture.impl.filter.NormalFilter
import com.lmy.codec.util.debug_v
import com.lmy.sample.helper.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener, SeekBar.OnSeekBarChangeListener {

    private lateinit var mPresenter: CameraPreviewPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        beautyBar.setOnSeekBarChangeListener(this)
        toneBar.setOnSeekBarChangeListener(this)
        brightBar.setOnSeekBarChangeListener(this)
        texelBar.setOnSeekBarChangeListener(this)
        loge("Permission: " + !PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
        if (!PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
            return
        mPresenter = CameraPreviewPresenter(com.lmy.codec.entity.Parameter(this))
        val mTextureView = TextureView(this)
        mTextureContainer.addView(mTextureView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))
        mTextureView.keepScreenOn = true
        mTextureView.surfaceTextureListener = this
        mTextureView.setOnTouchListener { v, event ->
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
            return@setOnTouchListener true
        }
        changeBtn.setOnClickListener({
            showFilterDialog()
        })
    }

    private fun showFilterDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Change filter")
            setItems(arrayOf("Normal", "Grey", "Beauty")) { dialog, which ->
                beautyLayout.visibility = if (2 == which) View.VISIBLE else View.GONE
                when (which) {
                    0 -> mPresenter.setFilter(NormalFilter::class.java)
                    1 -> mPresenter.setFilter(GreyFilter::class.java)
                    2 -> {
                        mPresenter.setFilter(BeautyFilter::class.java)
                        beautyBar.progress = 0
                        toneBar.progress = 50
                        brightBar.progress = 0
                        texelBar.progress = 50
                    }
                }
                dialog.dismiss()
            }
        }.show()
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
        debug_v("onSurfaceTextureAvailable")
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

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val filter = mPresenter.getFilter()
        if (filter is BeautyFilter) {
            when (seekBar.id) {
                R.id.beautyBar -> filter.setParams(beautyBar.progress / 100f * 2.5f,
                        (toneBar.progress - 50) / 100f * 10)
                R.id.toneBar -> filter.setParams(beautyBar.progress / 100f * 2.5f,
                        (toneBar.progress - 50) / 100f * 10)
                R.id.brightBar ->
                    filter.setBrightLevel(seekBar.progress / 100f)
                R.id.texelBar ->
                    filter.setBrightLevel((seekBar.progress - 50) / 100f * 2)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

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
}
