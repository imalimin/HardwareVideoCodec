package com.lmy.sample

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.View

/**
 * Created by lmyooyo@gmail.com on 2018/9/21.
 */
open class BaseActivity : AppCompatActivity() {

    fun fillStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }
}