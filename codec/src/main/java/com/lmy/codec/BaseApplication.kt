/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.AssetManager

/**
 * Created by 李明艺 on 2016/12/21.
 *
 * @author lrlmy@foxmail.com
 */

abstract class BaseApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        assetManager = base.assets
        assetManagerHashCode = baseContext!!.hashCode()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var assetManager: AssetManager? = null
        private var assetManagerHashCode: Int = 0

        fun assetManager(): AssetManager {
            return assetManager!!
        }
    }
}
