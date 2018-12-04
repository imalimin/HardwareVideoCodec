/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.sample

import android.app.Application

/**
 * Created by lmyooyo@gmail.com on 2018/5/29.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        CrashReport.initCrashReport(applicationContext, "d7f93990a9", false)
    }
}