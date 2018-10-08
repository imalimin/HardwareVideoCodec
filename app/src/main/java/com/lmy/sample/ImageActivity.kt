/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.sample

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.Toast
import com.lmy.codec.presenter.Processor
import com.lmy.codec.presenter.impl.ImageProcessorImpl
import kotlinx.android.synthetic.main.activity_image.*
import java.io.File


/**
 * Created by lmyooyo@gmail.com on 2018/9/21.
 */
class ImageActivity : BaseActivity() {
    private var mProcessor: Processor? = null
    private var mFilterController: FilterController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        fillStatusBar()
        initView()
    }

    private fun initView() {
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
        mProcessor = ImageProcessorImpl.create(applicationContext).apply {
            setPreviewDisplay(mTextureView)
            prepare()
        }
        mProcessor?.setInputResource(File(path))
        mFilterController = FilterController(mProcessor!!, progressLayout)
        effectBtn.setOnClickListener({
            mFilterController?.chooseFilter(this)
        })
        saveBtn.setOnClickListener {
            val out = getOutputPath(path!!)
            mProcessor?.save(out, Runnable {
                runOnUiThread {
                    Toast.makeText(this, "Saved to $out", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mProcessor?.release()
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
        return "${input.substring(0, input.lastIndexOf("."))}_${System.currentTimeMillis()}.jpg"
    }
}