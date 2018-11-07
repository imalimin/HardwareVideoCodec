package com.lmy.sample

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.lmy.codec.loge
import com.lmy.sample.helper.PermissionHelper

/**
 * Created by lmyooyo@gmail.com on 2018/9/21.
 */
open abstract class BaseActivity : AppCompatActivity() {
    abstract fun initView()
    abstract fun getLayoutResource(): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResource())
        fillStatusBar()
        loge("Permission: " + PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
        if (!PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
            return
        initView()
    }

    private fun showPermissionsDialog() {
        AlertDialog.Builder(this)
                .setMessage("Please grant permission in the permission settings")
                .setNegativeButton("cancel") { dialog, which -> finish() }
                .setPositiveButton("enter") { dialog, which ->
                    PermissionHelper.gotoPermissionManager(this@BaseActivity)
                    finish()
                }
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

    fun fillStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }
}