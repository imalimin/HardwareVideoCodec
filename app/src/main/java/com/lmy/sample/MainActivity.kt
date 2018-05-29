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
import android.widget.FrameLayout
import com.lmy.codec.CameraPreviewPresenter
import com.lmy.codec.loge
import com.lmy.codec.texture.impl.filter.BeautyTextureFilter
import com.lmy.codec.texture.impl.filter.GreyTextureFilter
import com.lmy.codec.texture.impl.filter.NormalTextureFilter
import com.lmy.codec.util.debug_v
import com.lmy.sample.helper.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private lateinit var mPresenter: CameraPreviewPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
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
                when (which) {
                    0 -> mPresenter.setFilter(NormalTextureFilter::class.java)
                    1 -> mPresenter.setFilter(GreyTextureFilter::class.java)
                    2 -> mPresenter.setFilter(BeautyTextureFilter::class.java)
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
