/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.sample

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.TextureView
import com.lmy.codec.entity.Parameter
import com.lmy.codec.CameraPreviewPresenter
import com.lmy.codec.util.debug_v
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private val mPresenter: CameraPreviewPresenter = CameraPreviewPresenter(Parameter(this))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
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
}
