/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.helper

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES20
import android.util.Log

/**
 * Created by lmyooyo@gmail.com on 2018/8/6.
 */
object GLHelper {
    init {
        System.loadLibrary("glhelper")
    }

    private val PBO_SUPPORT_VERSION = 0x30000

    external fun glReadPixels(x: Int,
                              y: Int,
                              width: Int,
                              height: Int,
                              format: Int,
                              type: Int)

    /**
     * @param context
     * @return hex
     */
    fun glVersion(context: Context): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo ?: return 0
        return info.reqGlEsVersion
    }

    fun isSupportPBO(context: Context): Boolean {
        return GLHelper.glVersion(context) > PBO_SUPPORT_VERSION
    }

    fun checkGLES2Error(tag: String): Int {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(tag, "glError 0x" + Integer.toHexString(error))
        }
        return error
    }
}