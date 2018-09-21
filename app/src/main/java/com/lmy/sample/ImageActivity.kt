/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.sample

import android.os.Bundle
import android.view.TextureView
import android.widget.FrameLayout
import com.lmy.codec.presenter.ImageProcessor
import com.lmy.codec.presenter.impl.ImageProcessorImpl
import kotlinx.android.synthetic.main.activity_image.*
import java.io.File

/**
 * Created by lmyooyo@gmail.com on 2018/9/21.
 */
class ImageActivity : BaseActivity() {
    private var mProcessor: ImageProcessor? = null
    private var mFilterController: FilterController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        fillStatusBar()
        initView()
    }

    private fun initView() {
        val mTextureView = TextureView(this).apply {
            fitsSystemWindows = true
            keepScreenOn = true
        }
        mTextureContainer.addView(mTextureView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))
        mProcessor = ImageProcessorImpl.create(applicationContext).apply {
            setPreviewDisplay(mTextureView)
            prepare()
        }
        mProcessor?.setInputImage(File("/sdcard/DSC00059.jpg"))
        mFilterController = FilterController(mProcessor!!, progressLayout)
        effectBtn.setOnClickListener({
            mFilterController?.chooseFilter(this)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mProcessor?.release()
    }
}