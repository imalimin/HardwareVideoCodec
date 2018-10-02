/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.sample

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioGroup
import com.lmy.codec.encoder.Encoder
import com.lmy.codec.loge
import com.lmy.codec.presenter.VideoRecorder
import com.lmy.codec.presenter.impl.VideoRecorderImpl
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.texture.impl.filter.BeautyV4Filter
import com.lmy.codec.texture.impl.filter.GroupFilter
import com.lmy.codec.texture.impl.sticker.ImageSticker
import com.lmy.codec.texture.impl.sticker.TextSticker
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CameraWrapper
import com.lmy.sample.helper.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(), View.OnTouchListener, RadioGroup.OnCheckedChangeListener {

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
        cameraGroup.setOnCheckedChangeListener(this)
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
            setOutputUri("${Environment.getExternalStorageDirectory().absolutePath}/test_${count++}.mp4")
//            setOutputUri("rtmp://192.168.16.203:1935/live/livestream")
            setOutputSize(720, 1280)//Default 720x1280
            setFps(30)
            enableHardware(true)
            setCameraIndex(CameraWrapper.CameraIndex.FRONT)
            setFilter(getDefaultFilter())
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
            mRecorder.setOutputUri("${Environment.getExternalStorageDirectory().absolutePath}/test_${count++}.mp4")
            mRecorder.prepare()
        }
    }

    private fun getDefaultFilter(): BaseFilter {
        return GroupFilter.create(BeautyV4Filter())
                .addSticker(TextSticker().apply {
                    setText(TextSticker.Text("HWVC", 56f).apply {
                        x = 0.8f
                        y = 0.03f
                    })
                })
                .addSticker(ImageSticker().apply {
                    setImage(ImageSticker.Image().apply {
                        x = 0.03f
                        y = 0.03f
                        scale = 1.6f
                        bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_logo_hwvc)
                    })
                })
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mRecorder.prepared())
                    mRecorder.start()
            }
            MotionEvent.ACTION_UP -> {
                if (mRecorder.started())
                    mRecorder.pause()
            }
        }
        return true
    }

    private var onStateListener =
            object : VideoRecorder.OnStateListener {
                override fun onError(error: Int, msg: String) {
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle("ERROR")
                            .setMessage(msg).show()
                }

                override fun onStop() {

                }

                override fun onPrepared(encoder: Encoder) {
//                    mRecorder.start()
                    runOnUiThread {
                        enableChangeRatio(true)
                        nextBtn.isEnabled = true
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
        when (group.id) {
            R.id.cameraGroup -> {
                val i = group.indexOfChild(group.findViewById(checkedId))
                if (0 == i)
                    mRecorder.setCameraIndex(CameraWrapper.CameraIndex.BACK)
                else
                    mRecorder.setCameraIndex(CameraWrapper.CameraIndex.FRONT)
            }
            R.id.ratioGroup -> {
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
                mRecorder.setOutputUri("${Environment.getExternalStorageDirectory().absolutePath}/test_${count++}.mp4")
                mRecorder.prepare()
            }
        }
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
}
